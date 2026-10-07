package com.smartcampus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.model.*;
import com.smartcampus.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCrudControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @Mock private RoomRepository roomRepository;
    @Mock private CampusNodeRepository campusNodeRepository;
    @Mock private CampusEdgeRepository campusEdgeRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRepository userRepository;

    private Room room;
    private CampusNode node;
    private CampusEdge edge;
    private Reservation reservation;
    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AdminRoomController(roomRepository, campusNodeRepository),
                new AdminCampusNodeController(campusNodeRepository),
                new AdminCampusEdgeController(campusEdgeRepository, campusNodeRepository),
                new AdminReservationController(reservationRepository, userRepository, roomRepository),
                new AdminRoleController(roleRepository),
                new AdminUserController(userRepository, roleRepository)
        ).build();

        node = new CampusNode();
        node.setId(1L);
        node.setBuildingId(1L);
        node.setFloorNumber(0);
        node.setCoordX(BigDecimal.ONE);
        node.setCoordY(BigDecimal.ONE);
        node.setNodeType("ROOM");
        node.setIsAccessible(true);

        room = new Room();
        room.setId(1L);
        room.setNodeId(1L);
        room.setRoomNumber("B1.01");
        room.setCapacity(6);
        room.setRoomType("STUDY_POD");

        edge = new CampusEdge();
        edge.setId(1L);
        edge.setSourceNodeId(1L);
        edge.setTargetNodeId(2L);
        edge.setWeightDistance(BigDecimal.ONE);
        edge.setRequiresStairs(false);

        role = new Role();
        role.setId(1L);
        role.setRoleName("ADMIN");

        user = new User();
        user.setId(1L);
        user.setName("Admin");
        user.setEmail("admin@example.com");
        user.setPasswordHash("hash");
        user.setRole(role);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(1L);
        reservation.setRoomId(1L);
        reservation.setStartTime(LocalDateTime.of(2026, 10, 7, 10, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 10, 7, 11, 0));
        reservation.setStatus("CONFIRMED");
    }

    @Test
    void roomCrudEndpoints() throws Exception {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findByRoomNumber("B1.01")).thenReturn(Optional.empty());
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(campusNodeRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.existsById(1L)).thenReturn(true);

        assertCrud("/api/admin/rooms", room);
        verify(roomRepository).deleteById(1L);
    }

    @Test
    void campusNodeCrudEndpoints() throws Exception {
        when(campusNodeRepository.findAll()).thenReturn(List.of(node));
        when(campusNodeRepository.findById(1L)).thenReturn(Optional.of(node));
        when(campusNodeRepository.save(any(CampusNode.class))).thenReturn(node);
        when(campusNodeRepository.existsById(1L)).thenReturn(true);

        assertCrud("/api/admin/campus-nodes", node);
        verify(campusNodeRepository).deleteById(1L);
    }

    @Test
    void campusEdgeCrudEndpoints() throws Exception {
        when(campusEdgeRepository.findAll()).thenReturn(List.of(edge));
        when(campusEdgeRepository.findById(1L)).thenReturn(Optional.of(edge));
        when(campusEdgeRepository.save(any(CampusEdge.class))).thenReturn(edge);
        when(campusEdgeRepository.existsById(1L)).thenReturn(true);
        when(campusNodeRepository.existsById(1L)).thenReturn(true);
        when(campusNodeRepository.existsById(2L)).thenReturn(true);

        assertCrud("/api/admin/campus-edges", edge);
        verify(campusEdgeRepository).deleteById(1L);
    }

    @Test
    void reservationCrudEndpoints() throws Exception {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.existsById(1L)).thenReturn(true);

        assertCrud("/api/admin/reservations", reservation);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void roleCrudEndpoints() throws Exception {
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleRepository.existsById(1L)).thenReturn(true);

        assertCrud("/api/admin/roles", role);
        verify(roleRepository).deleteById(1L);
    }

    @Test
    void userCrudEndpoints() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(roleRepository.existsById(1L)).thenReturn(true);

        assertCrud("/api/admin/users", user);
        verify(userRepository).deleteById(1L);
    }

    private void assertCrud(String basePath, Object entity) throws Exception {
        mockMvc.perform(get(basePath)).andExpect(status().isOk());
        mockMvc.perform(get(basePath + "/1")).andExpect(status().isOk());
        mockMvc.perform(post(basePath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isCreated());
        mockMvc.perform(put(basePath + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk());
        mockMvc.perform(delete(basePath + "/1")).andExpect(status().isNoContent());
    }
}
