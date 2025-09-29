const multer = require('multer');
const path = require('path');

// Use memory storage to store uploaded files temporarily
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });


require('dotenv').config();
const express = require('express');
const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// MySQL connection pool
const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10
});

// Test DB connection
pool.getConnection()
  .then(conn => {
    console.log('✅ MySQL connected successfully');
    conn.release();
  })
  .catch(err => {
    console.error('❌ MySQL connection failed:', err);
  });

// Routes
app.post('/api/register', async (req, res) => {
  const { name, phone, password } = req.body;
  try {
    const hashed = await bcrypt.hash(password, 10);
    const [result] = await pool.execute(
      'INSERT INTO users (name, phone, password) VALUES (?, ?, ?)',
      [name, phone, hashed]
    );
    res.json({ success: true, id: result.insertId });
  } catch (err) {
    console.error('MySQL Error:', err);
    res.status(500).json({ error: 'Database error' });
  }
});

app.post('/api/report', upload.single('file'), async (req, res) => {
    const { user_id, reportD } = req.body;
    const file = req.file; // file object from client, may be undefined

    if (!reportD || !user_id) {
        return res.status(400).json({ error: 'Missing report description or user ID' });
    }

    try {
        let file_name = null;
        let file_type = null;
        let file_data = null;

        if (file) { // Only process if a file is attached
            file_name = file.originalname;
            const ext = path.extname(file_name).toLowerCase();

            if (['.jpg', '.jpeg', '.png', '.gif'].includes(ext)) file_type = 'image';
            else if (['.mp4', '.avi', '.mov'].includes(ext)) file_type = 'video';
            else if (['.mp3', '.wav'].includes(ext)) file_type = 'audio';
            else file_type = 'text';

            file_data = file.buffer;
        }

        const [result] = await pool.execute(
            `INSERT INTO Report (user_id, reportD, file_name, file_type, file_data, status) 
             VALUES (?, ?, ?, ?, ?, ?)`,
            [user_id, reportD, file_name, file_type, file_data, 'pending']
        );

        res.json({ success: true, report_id: result.insertId });
    } catch (err) {
        console.error('MySQL Error:', err);
        res.status(500).json({ error: 'Database error' });
    }
});

// Route: Request Call Back
app.post('/api/request_callback', upload.none(), async (req, res) => {
    const { phone_no, status } = req.body;

    if (!phone_no) {
        return res.status(400).json({ error: 'Phone number is required' });
    }

    try {
        const [result] = await pool.execute(
            `INSERT INTO RequestCallback (phone_no, status) VALUES (?, ?)`,
            [phone_no, status || 'pending']
        );

        res.json({ success: true, request_id: result.insertId });
    } catch (err) {
        console.error('MySQL Error:', err);
        res.status(500).json({ error: 'Database error' });
    }
});

// Endpoint to receive live location
app.post('/api/track', async (req, res) => {
    const { phone_no, latitude, longitude } = req.body;

    if (!phone_no || latitude == null || longitude == null) {
        return res.status(400).json({ error: 'Missing data' });
    }

    try {
        const query = `
            INSERT INTO Track (phone_no, latitude, longitude)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE latitude = VALUES(latitude), longitude = VALUES(longitude)
        `;
        await pool.execute(query, [phone_no, latitude, longitude]);

        res.json({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Database error' });
    }
});



// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => console.log(`✅ Server running on port ${PORT}`));
