package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.model.SocietyRoom;
import com.mandal.service.SocietyRoomService;
import com.mandal.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Society Room (Vargani Tracker) controller.
 *
 * GET    /api/rooms              → list all rooms (with optional ?status=PENDING filter)
 * GET    /api/rooms/summary      → summary stats
 * GET    /api/rooms/{id}         → get single room
 * POST   /api/rooms              → add single room
 * POST   /api/rooms/bulk         → bulk add rooms
 * PUT    /api/rooms/{id}         → update room details
 * PUT    /api/rooms/{id}/status  → mark room status (PAID/PENDING/PARTIALLY_PAID)
 * DELETE /api/rooms/{id}         → delete room
 */
@WebServlet(urlPatterns = "/api/rooms/*")
public class SocietyRoomServlet extends HttpServlet {

    private final SocietyRoomService service = new SocietyRoomService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long mandalId = (Long) req.getAttribute("mandalId");
            String pathInfo = req.getPathInfo();

            // GET /api/rooms/summary
            if (pathInfo != null && pathInfo.equals("/summary")) {
                Map<String, Object> summary = service.getSummary(mandalId);
                JsonUtil.writeOk(resp, ApiResponse.ok(summary));
                return;
            }

            // GET /api/rooms/{id}
            if (pathInfo != null && pathInfo.length() > 1 && !pathInfo.contains("/")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                SocietyRoom room = service.getById(id, mandalId);
                if (room == null) {
                    JsonUtil.writeError(resp, 404, "Room not found");
                    return;
                }
                JsonUtil.writeOk(resp, ApiResponse.ok(room));
                return;
            }

            // GET /api/rooms?status=PENDING
            String status = req.getParameter("status");
            List<SocietyRoom> rooms = service.getAll(mandalId, status);
            JsonUtil.writeOk(resp, ApiResponse.ok(rooms));

        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid ID format");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");
            String pathInfo = req.getPathInfo();

            // POST /api/rooms/bulk
            if (pathInfo != null && pathInfo.equals("/bulk")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.readBody(req, Map.class);
                int roomStart = ((Number) body.get("roomStart")).intValue();
                int roomEnd = ((Number) body.get("roomEnd")).intValue();
                int floorStart = ((Number) body.get("floorStart")).intValue();
                int floorEnd = ((Number) body.get("floorEnd")).intValue();
                boolean includeOwner = Boolean.TRUE.equals(body.get("includeOwner"));

                int created = service.bulkAddRooms(roomStart, roomEnd, floorStart, floorEnd,
                        includeOwner, userId, userRole, mandalId);

                JsonUtil.writeResponse(resp, 201,
                        ApiResponse.ok(created + " rooms created", Map.of("count", created)));
                return;
            }

            // POST /api/rooms — single room
            SocietyRoom room = JsonUtil.readBody(req, SocietyRoom.class);
            SocietyRoom saved = service.addRoom(room, userId, userRole, mandalId);
            JsonUtil.writeResponse(resp, 201, ApiResponse.ok("Room added", saved));

        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeError(resp, 400, "Room ID required in path");
                return;
            }

            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");

            // PUT /api/rooms/{id}/status
            if (pathInfo.endsWith("/status")) {
                String idStr = pathInfo.substring(1, pathInfo.lastIndexOf("/status"));
                Long id = Long.parseLong(idStr);

                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.readBody(req, Map.class);
                String status = (String) body.get("status");
                BigDecimal amountPaid = body.get("amountPaid") != null
                        ? new BigDecimal(body.get("amountPaid").toString()) : null;
                String notes = (String) body.get("notes");

                SocietyRoom updated = service.markStatus(id, status, amountPaid, notes,
                        userId, userRole, mandalId);
                JsonUtil.writeOk(resp, ApiResponse.ok("Status updated", updated));
                return;
            }

            // PUT /api/rooms/{id} — update room details
            Long id = Long.parseLong(pathInfo.substring(1));
            SocietyRoom updates = JsonUtil.readBody(req, SocietyRoom.class);
            SocietyRoom updated = service.updateRoom(id, updates, userId, userRole, mandalId);
            JsonUtil.writeOk(resp, ApiResponse.ok("Room updated", updated));

        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid room ID");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                JsonUtil.writeError(resp, 400, "Room ID required in path");
                return;
            }

            Long id = Long.parseLong(pathInfo.substring(1));
            Long userId = (Long) req.getAttribute("userId");
            String userRole = (String) req.getAttribute("userRole");
            Long mandalId = (Long) req.getAttribute("mandalId");

            boolean deleted = service.deleteRoom(id, userId, userRole, mandalId);
            if (deleted) {
                JsonUtil.writeOk(resp, ApiResponse.ok("Room deleted", null));
            } else {
                JsonUtil.writeError(resp, 404, "Room not found");
            }
        } catch (SecurityException e) {
            JsonUtil.writeError(resp, 403, e.getMessage());
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "Invalid room ID");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
