package com.mandal.service;

import com.mandal.dao.SocietyRoomDao;
import com.mandal.model.SocietyRoom;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Business logic for society room tracking (vargani collection tracker).
 * Enforces role-based access: only ADMIN and KARYAKARTA can manage rooms.
 */
public class SocietyRoomService {

    private final SocietyRoomDao roomDao = new SocietyRoomDao();

    /**
     * Add a single room entry.
     */
    public SocietyRoom addRoom(SocietyRoom room, Long userId, String userRole, Long mandalId)
            throws SQLException {
        checkRole(userRole);
        validateRoom(room);

        room.setMandalId(mandalId);
        return roomDao.insert(room);
    }

    /**
     * Bulk-add rooms for initial society setup.
     * Creates rooms for the given range of room numbers and floors.
     * Floor 0 represents the room owner (landlord).
     */
    public int bulkAddRooms(int roomStart, int roomEnd, int floorStart, int floorEnd,
                            boolean includeOwner,
                            Long userId, String userRole, Long mandalId) throws SQLException {
        checkRole(userRole);

        if (roomStart <= 0 || roomEnd < roomStart) {
            throw new IllegalArgumentException("Invalid room range");
        }
        if (floorStart <= 0 || floorEnd < floorStart) {
            throw new IllegalArgumentException("Invalid floor range");
        }
        int totalFloors = floorEnd - floorStart + 1 + (includeOwner ? 1 : 0);
        if ((roomEnd - roomStart + 1) * totalFloors > 500) {
            throw new IllegalArgumentException("Cannot create more than 500 rooms at once");
        }

        List<SocietyRoom> rooms = new ArrayList<>();
        for (int r = roomStart; r <= roomEnd; r++) {
            // Add owner entry (floor 0) if requested
            if (includeOwner) {
                SocietyRoom owner = new SocietyRoom();
                owner.setMandalId(mandalId);
                owner.setRoomNumber(String.valueOf(r));
                owner.setFloorNumber(0);
                rooms.add(owner);
            }
            for (int f = floorStart; f <= floorEnd; f++) {
                SocietyRoom room = new SocietyRoom();
                room.setMandalId(mandalId);
                room.setRoomNumber(String.valueOf(r));
                room.setFloorNumber(f);
                rooms.add(room);
            }
        }

        return roomDao.insertBulk(rooms);
    }

    /**
     * Update a room's details (resident name, phone, notes, etc.).
     */
    public SocietyRoom updateRoom(Long roomId, SocietyRoom updates,
                                   Long userId, String userRole, Long mandalId) throws SQLException {
        checkRole(userRole);

        SocietyRoom existing = roomDao.findById(roomId, mandalId);
        if (existing == null) {
            throw new IllegalArgumentException("Room not found");
        }

        // Apply updates
        if (updates.getRoomNumber() != null) existing.setRoomNumber(updates.getRoomNumber());
        if (updates.getFloorNumber() > 0) existing.setFloorNumber(updates.getFloorNumber());
        if (updates.getResidentName() != null) existing.setResidentName(updates.getResidentName());
        if (updates.getResidentPhone() != null) existing.setResidentPhone(updates.getResidentPhone());
        if (updates.getNotes() != null) existing.setNotes(updates.getNotes());

        return roomDao.update(existing);
    }

    /**
     * Mark a room's vargani status (PAID, PENDING, PARTIALLY_PAID).
     */
    public SocietyRoom markStatus(Long roomId, String status, java.math.BigDecimal amountPaid,
                                   String notes, Long userId, String userRole, Long mandalId)
            throws SQLException {
        checkRole(userRole);

        SocietyRoom existing = roomDao.findById(roomId, mandalId);
        if (existing == null) {
            throw new IllegalArgumentException("Room not found");
        }

        // Validate status
        if (!"PAID".equals(status) && !"PENDING".equals(status) && !"PARTIALLY_PAID".equals(status)) {
            throw new IllegalArgumentException("Invalid status. Must be PAID, PENDING, or PARTIALLY_PAID");
        }

        existing.setVarganiStatus(status);
        if (amountPaid != null) existing.setAmountPaid(amountPaid);
        if ("PENDING".equals(status)) existing.setAmountPaid(java.math.BigDecimal.ZERO);
        if (notes != null) existing.setNotes(notes);
        existing.setMarkedBy(userId);
        existing.setMarkedAt(LocalDateTime.now());

        return roomDao.update(existing);
    }

    /**
     * Delete a room entry.
     */
    public boolean deleteRoom(Long roomId, Long userId, String userRole, Long mandalId)
            throws SQLException {
        checkRole(userRole);
        return roomDao.delete(roomId, mandalId);
    }

    /**
     * Get all rooms with optional status filter.
     */
    public List<SocietyRoom> getAll(Long mandalId, String status) throws SQLException {
        return roomDao.findAll(mandalId, status);
    }

    /**
     * Get summary stats.
     */
    public Map<String, Object> getSummary(Long mandalId) throws SQLException {
        return roomDao.getSummary(mandalId);
    }

    /**
     * Get a single room by ID.
     */
    public SocietyRoom getById(Long roomId, Long mandalId) throws SQLException {
        return roomDao.findById(roomId, mandalId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private void checkRole(String userRole) {
        if (!"ADMIN".equals(userRole) && !"KARYAKARTA".equals(userRole)) {
            throw new SecurityException("Only Admin or Karyakarta can manage room tracking");
        }
    }

    private void validateRoom(SocietyRoom room) {
        if (room.getRoomNumber() == null || room.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }
        if (room.getFloorNumber() < 0) {
            throw new IllegalArgumentException("Floor number must be 0 (owner) or positive");
        }
    }
}
