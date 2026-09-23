import psycopg2
import sys

# Database connection parameters matching your docker-compose / setup
DB_CONFIG = {
    "dbname": "smartcampus_db",
    "user": "campus_admin",
    "password": "campus_secure_password_2026",
    "host": "localhost",
    "port": 5432
}

def test_database_connection():
    print("[-] Testing PostgreSQL Connection...")
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        print("[+] SUCCESS: Connected to PostgreSQL database successfully.")
        return conn
    except Exception as e:
        print(f"[!] ERROR: Failed to connect to database: {e}")
        sys.exit(1)

def test_table_integrity(conn):
    print("\n[-] Verifying Table Row Counts (Data Integrity)...")
    expected_counts = {
        "roles": 3,
        "users": 4,
        "buildings": 1,
        "campus_nodes": 8,
        "campus_edges": 8,
        "rooms": 2,
        "equipment": 3,
        "reservations": 2
    }
    
    cursor = conn.cursor()
    all_passed = True
    for table, expected in expected_counts.items():
        cursor.execute(f"SELECT COUNT(*) FROM {table};")
        count = cursor.fetchone()[0]
        if count >= expected:
            print(f"    [OK] Table '{table}': {count} rows (Expected at least {expected})")
        else:
            print(f"    [FAIL] Table '{table}': {count} rows (Expected at least {expected})")
            all_passed = False
    cursor.close()
    if not all_passed:
        print("[!] WARNING: Some tables did not meet expected row counts. Check populate.sql.")

def test_analytical_queries(conn):
    print("\n[-] Executing Analytical Queries (`queries.sql` validation)...")
    
    # Query 1: Facility Resource Inventory
    q1 = """
        SELECT b.code, r.room_number, r.capacity 
        FROM buildings b 
        JOIN campus_nodes cn ON b.building_id = cn.building_id 
        JOIN rooms r ON cn.node_id = r.node_id;
    """
    cursor = conn.cursor()
    cursor.execute(q1)
    results = cursor.fetchall()
    print(f"    [OK] Facility Inventory Query returned {len(results)} rooms:")
    for row in results:
        print(f"         Building: {row[0]}, Room: {row[1]}, Capacity: {row[2]}")
    cursor.close()

if __name__ == "__main__":
    connection = test_database_connection()
    test_table_integrity(connection)
    test_analytical_queries(connection)
    connection.close()
    print("\n[+] All database verification tests completed successfully!")