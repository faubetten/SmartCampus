import requests
import time

BASE_URL = "http://localhost:8080"

def test_load_balancer_health():
    print("==================================================")
    print("TEST 1: Testing Nginx Load Balancer & Node Redundancy")
    print("==================================================")
    
    nodes_hit = set()
    for i in range(6):
        try:
            response = requests.get(f"{BASE_URL}/health", timeout=3)
            if response.status_code == 200:
                print(f"Request {i+1}: Status 200 -> {response.text}")
                nodes_hit.add(response.text)
            else:
                print(f"Request {i+1}: Status {response.status_code}")
        except requests.exceptions.ConnectionError:
            print(f"Request {i+1}: Connection failed. Is Docker Compose running?")
        time.sleep(0.5)
    
    if len(nodes_hit) > 1:
        print("\n[+] SUCCESS: Load balancing verified across multiple backend nodes!")
    else:
        print("\n[!] NOTICE: Responses came from a single node or check cluster configuration.")

def test_navigation_endpoint():
    print("\n==================================================")
    print("TEST 2: Testing A* Pathfinding Endpoint (/api/navigation/route)")
    print("==================================================")
    
    # Route from Node 1 (Entrance) to Node 3 (Room B1.01) using seed data
    url = f"{BASE_URL}/api/navigation/route?startId=1&targetId=3&accessibleOnly=false"
    
    try:
        response = requests.get(url, timeout=5)
        print(f"HTTP Status: {response.status_code}")
        if response.status_code == 200:
            route = response.json()
            print(f"[+] SUCCESS: Path found with {len(route)} nodes:")
            for node in route:
                print(f"    -> Node ID: {node['id']} ({node['nodeType']}) at Floor {node['floorNumber']}")
        else:
            print(f"[-] Response: {response.text}")
    except Exception as e:
        print(f"[!] Error calling navigation endpoint: {e}")

def test_scheduling_endpoint():
    print("\n==================================================")
    print("TEST 3: Testing CSP Room Allocation Endpoint (/api/scheduling/allocate)")
    print("==================================================")
    
    # Request a study pod for 4 people
    url = f"{BASE_URL}/api/scheduling/allocate?capacity=4&roomType=STUDY_POD&startTime=2026-10-01T10:00:00&endTime=2026-10-01T12:00:00"
    
    try:
        response = requests.post(url, timeout=5)
        print(f"HTTP Status: {response.status_code}")
        result = response.json()
        print(f"CSP Result: Success = {result.get('success')}")
        print(f"Message: {result.get('message')}")
        if result.get('assignedRoom'):
            room = result['assignedRoom']
            print(f"[+] SUCCESS: Allocated Room {room['roomNumber']} (Capacity: {room['capacity']})")
    except Exception as e:
        print(f"[!] Error calling scheduling endpoint: {e}")

if __name__ == "__main__":
    print("Starting SmartCampus End-to-End Endpoint Verification...")
    test_load_balancer_health()
    test_navigation_endpoint()
    test_scheduling_endpoint()
    print("\n==================================================")
    print("Verification complete!")