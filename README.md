# SmartCampus

## Admin CRUD API

All administration endpoints use the `/api/admin` prefix and require an
authenticated request. Each resource supports:

| Resource | Collection |
| --- | --- |
| Rooms | `/api/admin/rooms` |
| Campus nodes | `/api/admin/campus-nodes` |
| Campus edges | `/api/admin/campus-edges` |
| Reservations | `/api/admin/reservations` |
| Roles | `/api/admin/roles` |
| Users | `/api/admin/users` |

For every collection, use `GET` to list, `POST` to create, `GET /{id}` to
retrieve, `PUT /{id}` to update, and `DELETE /{id}` to remove a resource.
Create and update requests accept the JSON representation of the corresponding
Java model. Foreign-key references are checked before creating or updating
rooms, edges, reservations, and users. Duplicate room numbers, role names, and
user emails return `409 Conflict`.

## Running endpoint tests

From `backend`, run:

```text
mvn test
```

`AdminCrudControllerTest` exercises all five CRUD operations for every admin
resource, including the existing room controller.
