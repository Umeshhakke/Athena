from flask import Flask, render_template, jsonify, request
import mysql.connector

app = Flask(__name__)

db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'DATABASE_PASSWORD',
    'database': 'DATABASE_SCHEMA_NAME'
}

def fetch_table_data(query):
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor(dictionary=True)
    cursor.execute(query)
    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    return rows

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/data')
def get_data():
    report_data = fetch_table_data(
        "SELECT id, user_id, reportD, file_name, file_type, created_at, status FROM report"
    )
    track_data = fetch_table_data(
        "SELECT id, phone_no, latitude, longitude, updated_at FROM track"
    )
    callback_data = fetch_table_data(
        "SELECT id, phone_no, status FROM requestcallback"
    )
    return jsonify({'report': report_data, 'track': track_data, 'callback': callback_data})

@app.route('/api/resolve', methods=['POST'])
def resolve_status():
    data = request.get_json()
    table = data.get('table')
    record_id = data.get('id')

    if table not in ['report', 'requestcallback']:
        return jsonify({'error': 'Invalid table'}), 400

    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()
    cursor.execute(f"UPDATE {table} SET status='Resolved' WHERE id=%s", (record_id,))
    conn.commit()
    cursor.close()
    conn.close()
    return jsonify({'success': True})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
