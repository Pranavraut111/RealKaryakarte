import { useEffect, useState } from "react";
import { Search, Plus, Check, Clock, AlertCircle, ChevronRight, X, Download, Building2, Trash2, Edit2 } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import * as api from "@/api/index";
import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";

const inr = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

const statusConfig = {
  PAID:           { label: "paid",          bg: "bg-emerald-500/10", text: "text-emerald-600", icon: Check, dot: "bg-emerald-500" },
  PENDING:        { label: "pending",       bg: "bg-amber-500/10",   text: "text-amber-600",   icon: Clock, dot: "bg-amber-500" },
  PARTIALLY_PAID: { label: "partiallyPaid", bg: "bg-blue-500/10",    text: "text-blue-600",    icon: AlertCircle, dot: "bg-blue-500" },
};

/** Floor 0 = Room Owner (landlord), otherwise "Floor N" */
const floorLabel = (floorNumber, t) =>
  floorNumber === 0 ? (t("owner") || "Owner") : `${t("floor")} ${floorNumber}`;

export default function VarganiTrackerPage() {
  const { user } = useAuth();
  const { t } = useLang();
  const [rooms, setRooms] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [showBulkAdd, setShowBulkAdd] = useState(false);
  const [showAddRoom, setShowAddRoom] = useState(false);
  const [editRoom, setEditRoom] = useState(null);
  const [markRoom, setMarkRoom] = useState(null);

  const canEdit = user?.role === "ADMIN" || user?.role === "KARYAKARTA";

  const loadData = async () => {
    try {
      const params = filter !== "ALL" ? { status: filter } : {};
      const [roomsRes, summaryRes] = await Promise.all([
        api.getRooms(params),
        api.getRoomsSummary(),
      ]);
      setRooms(roomsRes.data || roomsRes || []);
      setSummary(summaryRes.data || summaryRes || {});
    } catch (err) {
      console.error("Failed to load tracker data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [filter]);

  const filtered = rooms.filter(
    (r) =>
      r.roomNumber?.toLowerCase().includes(search.toLowerCase()) ||
      r.residentName?.toLowerCase().includes(search.toLowerCase())
  );

  // Group rooms by room number for visual grouping
  const grouped = {};
  filtered.forEach((r) => {
    const key = r.roomNumber;
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(r);
  });

  const totalRooms = summary?.totalRooms ?? 0;
  const paidCount = summary?.paidCount ?? 0;
  const pendingCount = summary?.pendingCount ?? 0;
  const partialCount = summary?.partialCount ?? 0;
  const totalCollected = summary?.totalCollected ?? 0;
  const progress = totalRooms > 0 ? Math.round((paidCount / totalRooms) * 100) : 0;

  return (
    <AppShell title={t("varganiTracker")} subtitle={t("trackerSubtitle")}>
      {/* Summary Card */}
      <section className="ink-panel glow-accent relative overflow-hidden rounded-3xl p-6">
        <div className="pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-[var(--color-primary)] opacity-30 blur-3xl" />
        <div className="relative">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-[11px] font-medium uppercase tracking-[0.24em] text-ink-foreground/60">
                {t("trackCollection")}
              </p>
              <p className="font-display tabular mt-2 text-4xl font-semibold text-ink-foreground">
                {paidCount}/{totalRooms}
              </p>
              <p className="mt-1 text-sm text-ink-foreground/70">{t("roomsPaid")}</p>
            </div>
            <div className="flex flex-col items-center">
              {/* Circular progress */}
              <div className="relative h-20 w-20">
                <svg className="h-20 w-20 -rotate-90" viewBox="0 0 80 80">
                  <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor"
                    className="text-ink-foreground/10" strokeWidth="6" />
                  <circle cx="40" cy="40" r="34" fill="none" stroke="currentColor"
                    className="text-emerald-500" strokeWidth="6"
                    strokeLinecap="round"
                    strokeDasharray={`${2 * Math.PI * 34}`}
                    strokeDashoffset={`${2 * Math.PI * 34 * (1 - progress / 100)}`}
                    style={{ transition: "stroke-dashoffset 0.5s ease" }}
                  />
                </svg>
                <span className="absolute inset-0 flex items-center justify-center font-display text-lg font-bold text-ink-foreground">
                  {progress}%
                </span>
              </div>
            </div>
          </div>

          <div className="mt-5 grid grid-cols-3 gap-2">
            <div className="rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-3 py-2 text-center">
              <div className="flex items-center justify-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-emerald-500" />
                <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("paid")}</p>
              </div>
              <p className="tabular mt-0.5 text-lg font-semibold text-ink-foreground">{paidCount}</p>
            </div>
            <div className="rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-3 py-2 text-center">
              <div className="flex items-center justify-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-amber-500" />
                <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("pending")}</p>
              </div>
              <p className="tabular mt-0.5 text-lg font-semibold text-ink-foreground">{pendingCount}</p>
            </div>
            <div className="rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-3 py-2 text-center">
              <div className="flex items-center justify-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-blue-500" />
                <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("partiallyPaid")}</p>
              </div>
              <p className="tabular mt-0.5 text-lg font-semibold text-ink-foreground">{partialCount}</p>
            </div>
          </div>

          {totalCollected > 0 && (
            <div className="mt-3 rounded-xl border border-ink-foreground/10 bg-ink-foreground/6 px-4 py-2.5">
              <p className="text-[10px] uppercase tracking-wider text-ink-foreground/55">{t("totalRoomsCollected")}</p>
              <p className="tabular mt-0.5 text-lg font-semibold text-emerald-400">{inr(totalCollected)}</p>
            </div>
          )}
        </div>
      </section>

      {/* Action buttons */}
      {canEdit && (
        <section className="mt-4 grid grid-cols-2 gap-3">
          <button
            onClick={() => setShowBulkAdd(true)}
            className="flex min-h-[56px] items-center gap-2.5 whitespace-nowrap rounded-2xl px-4 py-3 text-sm font-medium transition-transform active:scale-[0.98] accent-gradient text-primary-foreground shadow-[var(--shadow-float)]"
          >
            <Building2 className="h-4 w-4" />
            {t("bulkAdd")}
            <Plus className="ml-auto h-4 w-4 opacity-70" />
          </button>
          <button
            onClick={() => {
              const today = new Date().toISOString().split("T")[0];
              api.downloadReport("2020-01-01", today).catch((err) => alert("Export failed: " + err.message));
            }}
            className="flex min-h-[56px] items-center gap-2.5 whitespace-nowrap rounded-2xl px-4 py-3 text-sm font-medium transition-transform active:scale-[0.98] surface-lift text-foreground"
          >
            <Download className="h-4 w-4" />
            {t("exportExcel")}
          </button>
        </section>
      )}

      {/* Filters + Search */}
      <section className="mt-6">
        <div className="mb-4 flex gap-2">
          {["ALL", "PAID", "PENDING", "PARTIALLY_PAID"].map((f) => {
            const active = filter === f;
            const lbl = f === "ALL" ? t("all") : t(statusConfig[f]?.label || f);
            return (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`rounded-full px-4 py-2 text-xs font-semibold transition-colors ${
                  active
                    ? "bg-primary text-primary-foreground"
                    : "bg-secondary text-muted-foreground hover:text-foreground"
                }`}
              >
                {lbl}
              </button>
            );
          })}
        </div>

        <div className="relative mb-4">
          <Search className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            placeholder={`${t("roomNumber")} / ${t("residentName")}...`}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-2xl border border-input bg-background/50 py-3.5 pl-12 pr-4 text-sm outline-none transition-colors focus:border-primary"
          />
        </div>
      </section>

      {/* Room List */}
      <section className="pb-24">
        {loading ? (
          <div className="flex h-40 items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-r-transparent" />
          </div>
        ) : Object.keys(grouped).length === 0 ? (
          <div className="surface-lift rounded-2xl px-6 py-14 text-center">
            <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-secondary text-muted-foreground">
              <Building2 className="h-6 w-6" />
            </div>
            <p className="font-display text-lg font-semibold">{t("noRoomsYet")}</p>
            <p className="mt-1 text-sm text-muted-foreground">{t("setupRoomsDesc")}</p>
            {canEdit && (
              <button
                onClick={() => setShowBulkAdd(true)}
                className="accent-gradient mt-4 inline-flex min-h-[44px] items-center gap-2 rounded-2xl px-6 text-sm font-semibold text-primary-foreground"
              >
                <Plus className="h-4 w-4" /> {t("bulkAdd")}
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {Object.entries(grouped).map(([roomNum, roomFloors]) => (
              <div key={roomNum} className="surface-lift overflow-hidden rounded-2xl">
                <div className="flex items-center gap-3 border-b border-border/50 bg-secondary/30 px-4 py-2.5">
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 font-display text-sm font-bold text-primary">
                    {roomNum}
                  </div>
                  <p className="text-sm font-semibold text-foreground">Room {roomNum}</p>
                  <p className="ml-auto text-xs text-muted-foreground">
                    {roomFloors.filter((f) => f.varganiStatus === "PAID").length}/{roomFloors.length}
                  </p>
                </div>
                <div className="divide-y divide-border/50">
                  {roomFloors.map((room) => {
                    const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;
                    const StatusIcon = cfg.icon;
                    return (
                      <div
                        key={room.id}
                        className="flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-secondary/20 transition-colors"
                        onClick={() => canEdit && setMarkRoom(room)}
                      >
                        <div className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${cfg.bg}`}>
                          <StatusIcon className={`h-4 w-4 ${cfg.text}`} />
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-medium text-foreground">
                            {room.residentName || <span className="text-muted-foreground italic">No name</span>}
                          </p>
                          <p className="text-[11px] text-muted-foreground">
                            {floorLabel(room.floorNumber, t)}
                            {room.residentPhone && ` · ${room.residentPhone}`}
                            {room.notes && ` · ${room.notes}`}
                          </p>
                        </div>
                        <div className="text-right flex items-center gap-2">
                          {room.amountPaid > 0 && (
                            <span className="tabular text-sm font-semibold text-emerald-600">
                              {inr(room.amountPaid)}
                            </span>
                          )}
                          <span className={`inline-flex rounded-full px-2.5 py-0.5 text-[10px] font-semibold ${cfg.bg} ${cfg.text}`}>
                            {t(cfg.label)}
                          </span>
                          {canEdit && <ChevronRight className="h-4 w-4 text-muted-foreground/40" />}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* FAB for adding a single room */}
      {canEdit && (
        <div className="fixed inset-x-0 bottom-24 z-40 pointer-events-none">
          <div className="relative mx-auto w-full max-w-xl px-5 sm:max-w-2xl">
            <button
              onClick={() => setShowAddRoom(true)}
              className="accent-gradient absolute right-5 bottom-0 flex h-14 w-14 items-center justify-center rounded-full text-white shadow-lg shadow-primary/30 transition-transform active:scale-95 pointer-events-auto"
              aria-label="Add Room"
            >
              <Plus className="h-6 w-6" />
            </button>
          </div>
        </div>
      )}

      {/* Bulk Add Sheet */}
      {showBulkAdd && (
        <BulkAddSheet
          onClose={() => setShowBulkAdd(false)}
          onSuccess={() => { setShowBulkAdd(false); loadData(); }}
        />
      )}

      {/* Add/Edit Room Sheet */}
      {(showAddRoom || editRoom) && (
        <AddRoomSheet
          initialData={editRoom}
          onClose={() => { setShowAddRoom(false); setEditRoom(null); }}
          onSuccess={() => { setShowAddRoom(false); setEditRoom(null); loadData(); }}
        />
      )}

      {/* Mark Status Sheet */}
      {markRoom && (
        <MarkStatusSheet
          room={markRoom}
          onClose={() => setMarkRoom(null)}
          onSuccess={() => { setMarkRoom(null); loadData(); }}
          onEdit={() => { setEditRoom(markRoom); setMarkRoom(null); }}
          onDelete={() => { setMarkRoom(null); loadData(); }}
        />
      )}
    </AppShell>
  );
}

// ─── Bulk Add Sheet ─────────────────────────────────────────────────────────

function BulkAddSheet({ onClose, onSuccess }) {
  const { t } = useLang();
  const [form, setForm] = useState({ roomStart: "", roomEnd: "", floorStart: "1", floorEnd: "3", includeOwner: true });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await api.bulkAddRooms({
        roomStart: Number(form.roomStart),
        roomEnd: Number(form.roomEnd),
        floorStart: Number(form.floorStart),
        floorEnd: Number(form.floorEnd),
        includeOwner: form.includeOwner,
      });
      setResult(res.data?.count || res.message);
      setTimeout(() => onSuccess(), 1200);
    } catch (err) {
      setError(err.message || "Failed to create rooms");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-end justify-center">
      <button onClick={onClose} className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in" />
      <div className="relative w-full max-w-xl animate-in slide-in-from-bottom-6 duration-300">
        <div className="glass rounded-t-3xl px-5 pb-8 pt-4">
          <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-muted-foreground/40" />
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-display text-lg font-semibold">{t("bulkAdd")}</h2>
            <button onClick={onClose} className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary">
              <X className="h-4 w-4" />
            </button>
          </div>

          {error && (
            <div className="mb-4 rounded-xl bg-destructive/10 p-3 text-sm font-medium text-destructive">{error}</div>
          )}

          {result ? (
            <div className="rounded-xl bg-emerald-500/10 p-4 text-center">
              <Check className="mx-auto h-8 w-8 text-emerald-600 mb-2" />
              <p className="text-sm font-semibold text-emerald-700">{result} rooms created!</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-foreground">{t("roomFrom")}</label>
                  <input required type="number" min="1" value={form.roomStart}
                    onChange={(e) => setForm({ ...form, roomStart: e.target.value })}
                    className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                    placeholder="1" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-foreground">{t("roomTo")}</label>
                  <input required type="number" min="1" value={form.roomEnd}
                    onChange={(e) => setForm({ ...form, roomEnd: e.target.value })}
                    className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                    placeholder="20" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-foreground">{t("floorFrom")}</label>
                  <input required type="number" min="1" value={form.floorStart}
                    onChange={(e) => setForm({ ...form, floorStart: e.target.value })}
                    className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                    placeholder="1" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-foreground">{t("floorTo")}</label>
                  <input required type="number" min="1" value={form.floorEnd}
                    onChange={(e) => setForm({ ...form, floorEnd: e.target.value })}
                    className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                    placeholder="3" />
                </div>
              </div>

              <label className="flex items-center gap-3 cursor-pointer rounded-xl bg-secondary/50 px-4 py-3">
                <input type="checkbox" checked={form.includeOwner}
                  onChange={(e) => setForm({ ...form, includeOwner: e.target.checked })}
                  className="h-5 w-5 rounded-md border-2 border-input accent-primary" />
                <span className="text-sm font-medium text-foreground">
                  👑 {t("includeOwner") || "Include Room Owner"}
                  <span className="block text-[11px] text-muted-foreground font-normal mt-0.5">
                    {t("includeOwnerDesc") || "Add an owner entry (floor 0) for each room — for cases where the landlord also pays vargani"}
                  </span>
                </span>
              </label>

              <p className="text-xs text-muted-foreground text-center">
                This will create {((Number(form.roomEnd) - Number(form.roomStart) + 1) * (Number(form.floorEnd) - Number(form.floorStart) + 1 + (form.includeOwner ? 1 : 0))) || 0} room entries{form.includeOwner ? " (incl. owner per room)" : ""}. Duplicates will be skipped.
              </p>

              <button type="submit" disabled={loading}
                className="accent-gradient mt-2 flex min-h-[48px] w-full items-center justify-center rounded-2xl text-sm font-semibold text-primary-foreground disabled:opacity-70">
                {loading ? t("creating") : t("createRooms")}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Add/Edit Room Sheet ────────────────────────────────────────────────────

function AddRoomSheet({ onClose, onSuccess, initialData }) {
  const { t } = useLang();
  const isEdit = !!initialData;
  const [isOwner, setIsOwner] = useState(initialData?.floorNumber === 0);
  const [form, setForm] = useState({
    roomNumber: initialData?.roomNumber || "",
    floorNumber: initialData?.floorNumber === 0 ? "0" : (initialData?.floorNumber || "1"),
    residentName: initialData?.residentName || "",
    residentPhone: initialData?.residentPhone || "",
    notes: initialData?.notes || "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const payload = { ...form, floorNumber: isOwner ? 0 : Number(form.floorNumber) };
      if (isEdit) {
        await api.updateRoom(initialData.id, payload);
      } else {
        await api.addRoom(payload);
      }
      onSuccess();
    } catch (err) {
      setError(err.message || "Failed to save room");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-end justify-center">
      <button onClick={onClose} className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in" />
      <div className="relative w-full max-w-xl animate-in slide-in-from-bottom-6 duration-300">
        <div className="glass rounded-t-3xl px-5 pb-8 pt-4">
          <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-muted-foreground/40" />
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-display text-lg font-semibold">{isEdit ? t("editRoom") : t("addRoom")}</h2>
            <button onClick={onClose} className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary">
              <X className="h-4 w-4" />
            </button>
          </div>

          {error && (
            <div className="mb-4 rounded-xl bg-destructive/10 p-3 text-sm font-medium text-destructive">{error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("roomNumber")}</label>
                <input required type="text" value={form.roomNumber}
                  onChange={(e) => setForm({ ...form, roomNumber: e.target.value })}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                  placeholder="4" />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("floor")}</label>
                {isOwner ? (
                  <div className="w-full rounded-2xl border border-primary/30 bg-primary/5 px-4 py-3 text-sm text-primary font-medium">
                    👑 {t("owner") || "Owner"}
                  </div>
                ) : (
                  <input required type="number" min="1" value={form.floorNumber}
                    onChange={(e) => setForm({ ...form, floorNumber: e.target.value })}
                    className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                    placeholder="1" />
                )}
              </div>
            </div>
            <label className="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" checked={isOwner}
                onChange={(e) => {
                  setIsOwner(e.target.checked);
                  if (e.target.checked) setForm({ ...form, floorNumber: "0" });
                  else setForm({ ...form, floorNumber: "1" });
                }}
                className="h-5 w-5 rounded-md border-2 border-input accent-primary" />
              <span className="text-sm font-medium text-foreground">👑 {t("owner") || "Owner"} <span className="text-muted-foreground font-normal text-xs">({t("roomOwnerDesc") || "Room owner / landlord"})</span></span>
            </label>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("residentName")}</label>
              <input type="text" value={form.residentName}
                onChange={(e) => setForm({ ...form, residentName: e.target.value })}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                placeholder="Jadhav Family" />
            </div>
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("residentPhone")}</label>
              <input type="tel" value={form.residentPhone}
                onChange={(e) => setForm({ ...form, residentPhone: e.target.value })}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                placeholder="9876543210" />
            </div>

            <button type="submit" disabled={loading}
              className="accent-gradient mt-2 flex min-h-[48px] w-full items-center justify-center rounded-2xl text-sm font-semibold text-primary-foreground disabled:opacity-70">
              {loading ? t("saving") : isEdit ? t("saveChanges") : t("addRoom")}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

// ─── Mark Status Sheet ──────────────────────────────────────────────────────

function MarkStatusSheet({ room, onClose, onSuccess, onEdit, onDelete }) {
  const { t } = useLang();
  const [status, setStatus] = useState(room.varganiStatus || "PENDING");
  const [amountPaid, setAmountPaid] = useState(room.amountPaid || "");
  const [notes, setNotes] = useState(room.notes || "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [deleting, setDeleting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api.markRoomStatus(room.id, {
        status,
        amountPaid: amountPaid ? Number(amountPaid) : 0,
        notes: notes || null,
      });
      onSuccess();
    } catch (err) {
      setError(err.message || "Failed to update status");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm("Delete this room entry?")) return;
    setDeleting(true);
    try {
      await api.deleteRoom(room.id);
      onDelete();
    } catch (err) {
      setError(err.message || "Failed to delete");
    } finally {
      setDeleting(false);
    }
  };

  const cfg = statusConfig[room.varganiStatus] || statusConfig.PENDING;

  return (
    <div className="fixed inset-0 z-[60] flex items-end justify-center">
      <button onClick={onClose} className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in" />
      <div className="relative w-full max-w-xl animate-in slide-in-from-bottom-6 duration-300">
        <div className="glass rounded-t-3xl px-5 pb-8 pt-4">
          <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-muted-foreground/40" />

          {/* Room info header */}
          <div className="mb-5 flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 font-display text-lg font-bold text-primary">
              {room.roomNumber}
            </div>
            <div className="flex-1">
              <p className="font-semibold text-foreground">
                Room {room.roomNumber} · {floorLabel(room.floorNumber, t)}
              </p>
              <p className="text-sm text-muted-foreground">
                {room.residentName || "No resident name"}
              </p>
            </div>
            <div className="flex gap-1.5">
              <button onClick={onEdit}
                className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary text-muted-foreground hover:text-primary transition-colors">
                <Edit2 className="h-4 w-4" />
              </button>
              <button onClick={handleDelete} disabled={deleting}
                className="flex h-9 w-9 items-center justify-center rounded-full bg-destructive/10 text-destructive hover:bg-destructive/20 transition-colors">
                <Trash2 className="h-4 w-4" />
              </button>
              <button onClick={onClose}
                className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary">
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>

          {error && (
            <div className="mb-4 rounded-xl bg-destructive/10 p-3 text-sm font-medium text-destructive">{error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Status pills */}
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("status")}</label>
              <div className="flex gap-2">
                {[
                  { val: "PAID", label: t("paid"), color: "emerald" },
                  { val: "PARTIALLY_PAID", label: t("partiallyPaid"), color: "blue" },
                  { val: "PENDING", label: t("pending"), color: "amber" },
                ].map(({ val, label, color }) => (
                  <button
                    key={val}
                    type="button"
                    onClick={() => setStatus(val)}
                    className={`flex-1 rounded-xl py-2.5 text-xs font-semibold transition-all ${
                      status === val
                        ? `bg-${color}-500 text-white shadow-lg`
                        : `bg-${color}-500/10 text-${color}-600`
                    }`}
                    style={status === val ? {
                      backgroundColor: color === "emerald" ? "#10b981" : color === "blue" ? "#3b82f6" : "#f59e0b",
                      color: "white",
                    } : {}}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>

            {/* Amount */}
            {status !== "PENDING" && (
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("amountPaid")} (₹)</label>
                <input type="number" min="0" value={amountPaid}
                  onChange={(e) => setAmountPaid(e.target.value)}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                  placeholder="501" />
              </div>
            )}

            {/* Notes */}
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("addNotes")}</label>
              <input type="text" value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                placeholder="Will pay next week..." />
            </div>

            <button type="submit" disabled={loading}
              className="accent-gradient mt-2 flex min-h-[48px] w-full items-center justify-center rounded-2xl text-sm font-semibold text-primary-foreground disabled:opacity-70">
              {loading ? t("saving") : t("updateStatus")}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
