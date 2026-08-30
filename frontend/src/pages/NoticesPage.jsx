import { useEffect, useState, useRef } from "react";
import { Pin, Plus, Trash2, MessageCircle, Send, Image, X, Share2, AlertCircle, CheckCircle2, Clock } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import * as api from "@/api/index";
import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";

const REACTIONS = ["👍", "❤️", "🙏", "🎉", "😂"];

function timeAgo(dateStr) {
  if (!dateStr) return "";
  const parsedStr = dateStr.endsWith('Z') ? dateStr : dateStr + 'Z';
  const now = new Date();
  const d = new Date(parsedStr);
  const diff = Math.floor((now - d) / 1000);
  if (diff < 60) return "just now";
  if (diff < 3600) return Math.floor(diff / 60) + "m ago";
  if (diff < 86400) return Math.floor(diff / 3600) + "h ago";
  if (diff < 604800) return Math.floor(diff / 86400) + "d ago";
  return d.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
}

/* ─── Comment Section ─────────────────────────────────────────────── */
function CommentSection({ noticeId, user }) {
  const { t } = useLang();
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const inputRef = useRef(null);

  const fetchComments = async () => {
    setLoading(true);
    try {
      const res = await api.getComments(noticeId);
      setComments(res.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComments();
    setTimeout(() => inputRef.current?.focus(), 100);
  }, [noticeId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || submitting) return;
    setSubmitting(true);
    try {
      await api.addComment(noticeId, newComment.trim());
      setNewComment("");
      fetchComments();
    } catch (err) {
      alert("Failed: " + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId) => {
    try {
      await api.deleteComment(noticeId, commentId);
      fetchComments();
    } catch (err) {
      alert("Failed: " + err.message);
    }
  };

  return (
    <div className="mt-4 border-t border-border pt-4">
      <form onSubmit={handleSubmit} className="flex items-center gap-2 mb-3">
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary uppercase">
          {user?.name?.charAt(0) || "?"}
        </div>
        <input
          ref={inputRef}
          type="text"
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder={t("writeReply")}
          className="flex-1 rounded-full border border-input bg-background/50 px-4 py-2 text-sm outline-none focus:border-primary placeholder:text-muted-foreground transition-colors"
        />
        <button
          type="submit"
          disabled={!newComment.trim() || submitting}
          className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-white disabled:opacity-30 transition-all hover:scale-105 active:scale-95"
        >
          <Send className="h-3.5 w-3.5" />
        </button>
      </form>

      {loading ? (
        <p className="text-xs text-muted-foreground text-center py-2">{t("loading")}</p>
      ) : comments.length === 0 ? (
        <p className="text-xs text-muted-foreground text-center py-2">{t("noRepliesYet")}</p>
      ) : (
        <div className="space-y-3 max-h-72 overflow-y-auto pr-1">
          {comments.map((c) => (
            <div key={c.id} className="flex gap-2 group">
              <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-secondary text-[10px] font-bold text-foreground mt-0.5 uppercase">
                {c.userName?.charAt(0) || "?"}
              </div>
              <div className="flex-1 min-w-0">
                <div className="inline-block rounded-2xl bg-secondary/60 px-3 py-2 max-w-full">
                  <p className="text-xs font-semibold text-foreground">{c.userName}</p>
                  <p className="text-sm text-foreground/90 break-words whitespace-pre-wrap">{c.body}</p>
                </div>
                <div className="flex items-center gap-3 mt-0.5 px-1">
                  <span className="text-[10px] text-muted-foreground">{timeAgo(c.createdAt)}</span>
                  <button
                    onClick={() => {
                      setNewComment(`@${c.userName} `);
                      inputRef.current?.focus();
                    }}
                    className="text-[10px] font-medium text-muted-foreground hover:text-primary transition-colors"
                  >
                    Reply
                  </button>
                  {(c.userId === user?.id || user?.role === "ADMIN") && (
                    <button
                      onClick={() => handleDelete(c.id)}
                      className="text-[10px] font-medium text-muted-foreground hover:text-destructive transition-colors"
                    >
                      Delete
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* ─── Reaction Picker ─────────────────────────────────────────────── */
function ReactionBar({ noticeId, reactions, myReaction, onToggle }) {
  const { t } = useLang();
  const [showPicker, setShowPicker] = useState(false);
  const pickerRef = useRef(null);

  const total = reactions ? Object.values(reactions).reduce((a, b) => a + b, 0) : 0;

  const handleReact = async (emoji) => {
    setShowPicker(false);
    onToggle(noticeId, emoji);
  };

  useEffect(() => {
    const handler = (e) => {
      if (pickerRef.current && !pickerRef.current.contains(e.target)) {
        setShowPicker(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  return (
    <div className="relative inline-flex items-center" ref={pickerRef}>
      {reactions && Object.entries(reactions).map(([emoji, count]) => (
        <button
          key={emoji}
          onClick={() => handleReact(emoji)}
          className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 mr-1 text-xs font-medium transition-all active:scale-95 ${
            myReaction === emoji
              ? "bg-primary/15 text-primary ring-1 ring-primary/30"
              : "bg-secondary/60 text-foreground hover:bg-secondary"
          }`}
        >
          <span>{emoji}</span>
          <span>{count}</span>
        </button>
      ))}

      <button
        onClick={() => setShowPicker(!showPicker)}
        className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1.5 text-xs transition-all ${
          showPicker
            ? "bg-primary/10 text-primary"
            : "text-muted-foreground hover:bg-secondary hover:text-foreground"
        }`}
      >
        {total === 0 && <span>😊</span>}
        <span>{total === 0 ? "React" : "+"}</span>
      </button>

      {showPicker && (
        <div className="absolute bottom-full left-0 mb-2 flex gap-1 rounded-2xl bg-popover border border-border shadow-xl p-2 z-50 animate-in fade-in slide-in-from-bottom-2">
          {REACTIONS.map((emoji) => (
            <button
              key={emoji}
              onClick={() => handleReact(emoji)}
              className={`text-xl p-1.5 rounded-xl transition-all hover:scale-125 hover:bg-secondary active:scale-95 ${
                myReaction === emoji ? "bg-primary/15 scale-110" : ""
              }`}
            >
              {emoji}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

/* ─── WhatsApp Share ──────────────────────────────────────────────── */
function shareToWhatsApp(notice) {
  let text = "";
  if (notice.isPinned) text += "[PINNED]\n\n";
  text += `*${notice.title}*\n\n`;
  text += `${notice.body}\n\n`;
  text += `— ${notice.postedByName || "Admin"}\n`;
  text += `Date: ${new Date(notice.createdAt.endsWith('Z') ? notice.createdAt : notice.createdAt + 'Z').toLocaleDateString("en-IN", { day: "numeric", month: "long", year: "numeric" })}\n\n`;

  const url = `https://wa.me/?text=${encodeURIComponent(text)}`;
  window.open(url, "_blank");
}

/* ─── Main Page ───────────────────────────────────────────────────── */
export default function NoticesPage() {
  const { user } = useAuth();
  const { t, lang } = useLang();
  
  // Tabs for Admin/Karyakarta: "notices" | "complaints"
  const [activeTab, setActiveTab] = useState("notices");

  // Notices State
  const [notices, setNotices] = useState([]);
  const [reactionData, setReactionData] = useState({});
  const [userReactions, setUserReactions] = useState({});
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [expandedComments, setExpandedComments] = useState({});

  // Notices Form State
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [isPinned, setIsPinned] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [posting, setPosting] = useState(false);

  // Complaints State
  const [complaints, setComplaints] = useState([]);
  const [showComplaintForm, setShowComplaintForm] = useState(false);
  const [complaintBody, setComplaintBody] = useState("");
  const [submittingComplaint, setSubmittingComplaint] = useState(false);

  const canManage = user?.role === "ADMIN" || user?.role === "KARYAKARTA";
  const isAdmin = user?.role === "ADMIN";

  // --- Fetches ---
  const fetchNotices = () => {
    setLoading(true);
    api.getNotices()
      .then((res) => {
        const data = res.data || res;
        if (data.notices) {
          setNotices(data.notices);
          setReactionData(data.reactions || {});
          setUserReactions(data.userReactions || {});
        } else {
          setNotices(Array.isArray(data) ? data : []);
        }
      })
      .catch((err) => console.error("Failed to load notices:", err))
      .finally(() => setLoading(false));
  };

  const fetchComplaints = () => {
    setLoading(true);
    api.getComplaints()
      .then((res) => setComplaints(res.data || []))
      .catch((err) => console.error("Failed to load complaints:", err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (activeTab === "notices") fetchNotices();
    if (activeTab === "complaints") fetchComplaints();
  }, [activeTab]);

  // --- Notice Actions ---
  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setImagePreview(URL.createObjectURL(file));
    }
  };

  const removeImage = () => {
    setImageFile(null);
    setImagePreview(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !body.trim()) return;
    setPosting(true);

    try {
      let photoUrl = null;
      if (imageFile) {
        const uploadRes = await api.uploadFile(imageFile);
        photoUrl = uploadRes.data || uploadRes;
      }
      await api.addNotice({ title, body, isPinned, photoUrl });
      setTitle("");
      setBody("");
      setIsPinned(false);
      removeImage();
      setShowForm(false);
      fetchNotices();
    } catch (err) {
      alert("Failed to post: " + err.message);
    } finally {
      setPosting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Delete this post and all its comments?")) return;
    try {
      await api.deleteNotice(id);
      fetchNotices();
    } catch (err) {
      alert("Failed: " + err.message);
    }
  };

  const handleReaction = async (noticeId, emoji) => {
    try {
      const res = await api.toggleReaction(noticeId, emoji);
      const { added, summary } = res.data;
      setReactionData(prev => ({ ...prev, [noticeId]: summary }));
      setUserReactions(prev => {
        const copy = { ...prev };
        if (added) copy[noticeId] = emoji;
        else delete copy[noticeId];
        return copy;
      });
    } catch (err) {
      console.error("Reaction failed:", err);
    }
  };

  const toggleComments = (id) => {
    setExpandedComments(prev => ({ ...prev, [id]: !prev[id] }));
  };

  // --- Complaint Actions ---
  const handleComplaintSubmit = async (e) => {
    e.preventDefault();
    if (!complaintBody.trim()) return;
    setSubmittingComplaint(true);
    try {
      await api.addComplaint(complaintBody);
      setComplaintBody("");
      setShowComplaintForm(false);
      fetchComplaints();
      alert("Your suggestion has been submitted anonymously. Thank you!");
    } catch (err) {
      alert("Failed to submit: " + err.message);
    } finally {
      setSubmittingComplaint(false);
    }
  };

  const handleResolveComplaint = async (id) => {
    try {
      await api.resolveComplaint(id);
      fetchComplaints(); // refresh list
    } catch (err) {
      alert("Failed to resolve: " + err.message);
    }
  };

  const sortedNotices = [...notices].sort((a, b) => Number(b.isPinned) - Number(a.isPinned));
  const pendingComplaints = complaints.filter(c => c.status === 'PENDING').length;

  return (
    <AppShell title={t("community")} subtitle={t("communitySubtitle")}>

      {/* ─── Top Bar / Tabs ──────────────────────────────────────── */}
      <div className="mb-6 flex items-center justify-between">
        <div className="flex bg-secondary/50 rounded-full p-1 border border-border">
          <button
            onClick={() => setActiveTab("notices")}
            className={`px-4 py-1.5 text-sm font-medium rounded-full transition-all ${
              activeTab === "notices" ? "bg-background shadow text-foreground" : "text-muted-foreground hover:text-foreground"
            }`}
          >
            {t("noticeBoard")}
          </button>
          <button
            onClick={() => setActiveTab("complaints")}
            className={`px-4 py-1.5 text-sm font-medium rounded-full transition-all flex items-center gap-1.5 ${
              activeTab === "complaints" ? "bg-background shadow text-foreground" : "text-muted-foreground hover:text-foreground"
            }`}
          >
            {t("inbox")}
            {pendingComplaints > 0 && (
              <span className="flex h-4 min-w-[16px] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
                {pendingComplaints}
              </span>
            )}
          </button>
        </div>

        {/* Suggestion Box Button for Everyone */}
        {activeTab === "notices" && (
          <button
            onClick={() => setShowComplaintForm(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-orange-600 bg-orange-500/10 hover:bg-orange-500/20 rounded-full transition-colors"
          >
            <AlertCircle className="h-4 w-4" />
            <span>{t("anonymousSuggestion")}</span>
          </button>
        )}
      </div>

      {/* ─── Anonymous Complaint Form Modal ──────────────────────── */}
      {showComplaintForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-in fade-in">
          <div className="w-full max-w-md bg-background rounded-3xl p-6 shadow-2xl border border-border animate-in zoom-in-95">
            <h3 className="font-display text-lg font-semibold text-foreground mb-1">{t("suggestionBox")}</h3>
            <p className="text-sm text-muted-foreground mb-4">
              {t("suggestionBoxDesc")}
            </p>
            <form onSubmit={handleComplaintSubmit}>
              <textarea
                placeholder={t("typeMessageHere")}
                value={complaintBody}
                onChange={(e) => setComplaintBody(e.target.value)}
                className="w-full h-32 rounded-2xl border border-input bg-secondary/30 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-orange-500 resize-none mb-4"
                required
              />
              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowComplaintForm(false)}
                  className="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground"
                >
                  {t("cancel")}
                </button>
                <button
                  type="submit"
                  disabled={submittingComplaint}
                  className="bg-orange-500 hover:bg-orange-600 text-white px-5 py-2 rounded-xl text-sm font-semibold transition-colors disabled:opacity-50"
                >
                  {submittingComplaint ? t("sending") : t("sendSecurely")}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ─── TAB: COMPLAINTS (ADMIN/KARYAKARTA) ──────────────────── */}
      {activeTab === "complaints" && (
        <div className="space-y-4">
          {loading ? (
             <div className="flex justify-center py-10"><div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" /></div>
          ) : complaints.length === 0 ? (
            <div className="text-center py-14">
              <p className="text-muted-foreground text-sm">{t("noComplaintsYet")}</p>
            </div>
          ) : (
            complaints.map(c => (
              <div key={c.id} className={`surface-lift p-5 rounded-2xl border ${c.status === 'RESOLVED' ? 'opacity-60 border-border' : 'border-orange-500/30 shadow-sm'}`}>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-bold text-orange-600 bg-orange-600/10 px-2 py-0.5 rounded uppercase tracking-wider">
                        {t("anonymous")}
                      </span>
                      <span className="text-xs text-muted-foreground">{timeAgo(c.createdAt)}</span>
                    </div>
                    <p className="text-sm text-foreground whitespace-pre-wrap">{c.message}</p>
                  </div>
                  {c.status === 'PENDING' ? (
                    canManage ? (
                      <button
                        onClick={() => handleResolveComplaint(c.id)}
                        className="shrink-0 flex items-center gap-1.5 bg-green-500/10 text-green-600 hover:bg-green-500/20 px-3 py-1.5 rounded-full text-xs font-semibold transition-colors"
                      >
                        <CheckCircle2 className="h-4 w-4" />
                        {t("resolve")}
                      </button>
                    ) : (
                      <span className="shrink-0 text-xs font-medium text-orange-500 flex items-center gap-1">
                        <Clock className="h-3.5 w-3.5" /> {lang === "mr" ? "प्रलंबित" : "Pending"}
                      </span>
                    )
                  ) : (
                    <span className="shrink-0 text-xs font-medium text-muted-foreground flex items-center gap-1">
                      <CheckCircle2 className="h-3.5 w-3.5" /> {t("resolved")}
                    </span>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* ─── TAB: NOTICES (DEFAULT) ──────────────────────────────── */}
      {activeTab === "notices" && (
        <>
          {showForm && (
            <div className="surface-lift rounded-3xl p-5 mb-6 border border-primary/20">
              <h2 className="font-display font-semibold text-foreground mb-4">{t("newPost")}</h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <input
                  type="text"
                  placeholder={t("whatsTheTitle")}
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground"
                  required
                />
                <textarea
                  placeholder={t("shareSomething")}
                  value={body}
                  onChange={(e) => setBody(e.target.value)}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm text-foreground outline-none transition-colors focus:border-primary placeholder:text-muted-foreground min-h-[100px] resize-none"
                  required
                />

                {imagePreview && (
                  <div className="relative rounded-2xl overflow-hidden">
                    <img src={imagePreview} alt="Preview" className="w-full max-h-64 object-cover rounded-2xl" />
                    <button
                      type="button"
                      onClick={removeImage}
                      className="absolute top-2 right-2 flex h-8 w-8 items-center justify-center rounded-full bg-black/60 text-white hover:bg-black/80 transition-colors"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                )}

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <label className="flex items-center gap-1.5 cursor-pointer text-sm text-muted-foreground hover:text-primary transition-colors">
                      <Image className="h-5 w-5" />
                      <span>{t("photo")}</span>
                      <input type="file" accept="image/*" onChange={handleImageSelect} className="hidden" />
                    </label>

                    <label className="flex items-center gap-2 text-sm text-muted-foreground">
                      <input
                        type="checkbox"
                        checked={isPinned}
                        onChange={(e) => setIsPinned(e.target.checked)}
                        className="rounded"
                      />
                      <Pin className="h-3.5 w-3.5" /> {t("pinToTop")}
                    </label>
                  </div>

                  <div className="flex gap-3">
                    <button
                      type="button"
                      onClick={() => { setShowForm(false); removeImage(); }}
                      className="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={posting}
                      className="accent-gradient min-h-[40px] px-6 rounded-xl text-sm font-semibold text-primary-foreground disabled:opacity-50"
                    >
                      {posting ? t("posting") : t("post")}
                    </button>
                  </div>
                </div>
              </form>
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            </div>
          ) : sortedNotices.length === 0 ? (
            <div className="surface-lift rounded-3xl px-6 py-14 text-center">
              <p className="font-display text-lg font-semibold">{t("noPostsYet")}</p>
              <p className="mt-1 text-sm text-muted-foreground">{t("beTheFirst")}</p>
            </div>
          ) : (
            <div className="space-y-4">
              {sortedNotices.map((n) => {
                const nReactions = reactionData[n.id] || {};
                const myReaction = userReactions[n.id] || null;

                return (
                  <article
                    key={n.id}
                    className={`surface-lift rounded-3xl overflow-hidden transition-shadow hover:shadow-md ${
                      n.isPinned ? "ring-1 ring-primary/20" : ""
                    }`}
                  >
                    {n.isPinned && <div className="accent-gradient h-1 w-full" />}

                    <div className="p-5">
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center gap-3">
                          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 font-display font-bold text-primary text-sm uppercase">
                            {n.postedByName?.charAt(0) || "A"}
                          </div>
                          <div>
                            <p className="text-sm font-semibold text-foreground leading-tight">{n.postedByName || "Admin"}</p>
                            <p className="text-[11px] text-muted-foreground">{timeAgo(n.createdAt)}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          {n.isPinned && (
                            <span className="inline-flex items-center gap-1 rounded-full bg-primary/10 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wider text-primary">
                              <Pin className="h-3 w-3" /> {t("pinned")}
                            </span>
                          )}
                          {isAdmin && (
                            <button
                              onClick={() => handleDelete(n.id)}
                              className="text-muted-foreground hover:text-destructive p-1.5 rounded-full hover:bg-destructive/10 transition-colors"
                            >
                              <Trash2 size={15} />
                            </button>
                          )}
                        </div>
                      </div>

                      <h2 className="font-display text-base font-bold leading-snug text-foreground">{n.title}</h2>
                      <p className="mt-2 text-sm leading-relaxed text-muted-foreground whitespace-pre-wrap">{n.body}</p>

                      {n.photoUrl && (
                        <div className="mt-3 rounded-2xl overflow-hidden">
                          <img
                            src={api.getMediaUrl(n.photoUrl)}
                            alt={n.title}
                            className="w-full max-h-80 object-cover cursor-pointer hover:opacity-95 transition-opacity"
                            onClick={() => window.open(api.getMediaUrl(n.photoUrl), "_blank")}
                          />
                        </div>
                      )}

                      <div className="mt-3 flex flex-wrap items-center gap-1">
                        <ReactionBar
                          noticeId={n.id}
                          reactions={nReactions}
                          myReaction={myReaction}
                          onToggle={handleReaction}
                        />
                      </div>

                      <div className="mt-2 flex items-center gap-1 border-t border-border pt-3">
                        <button
                          onClick={() => toggleComments(n.id)}
                          className={`flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm transition-colors ${
                            expandedComments[n.id]
                              ? "bg-primary/10 text-primary font-medium"
                              : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                          }`}
                        >
                          <MessageCircle className="h-4 w-4" />
                          <span>Reply</span>
                        </button>

                        <button
                          onClick={() => shareToWhatsApp(n)}
                          className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm text-muted-foreground hover:bg-green-500/10 hover:text-green-600 transition-colors"
                        >
                          <Share2 className="h-4 w-4" />
                          <span>{t("whatsapp")}</span>
                        </button>
                      </div>

                      {expandedComments[n.id] && (
                        <CommentSection noticeId={n.id} user={user} />
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          )}

          {canManage && !showForm && (
            <div className="fixed inset-x-0 bottom-24 z-40 pointer-events-none">
              <div className="relative mx-auto w-full max-w-xl px-5 sm:max-w-2xl">
                <button
                  onClick={() => setShowForm(true)}
                  className="accent-gradient absolute right-5 bottom-0 flex h-14 w-14 items-center justify-center rounded-full text-white shadow-lg shadow-primary/30 transition-transform active:scale-95 pointer-events-auto"
                  aria-label="New Post"
                >
                  <Plus className="h-6 w-6" />
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </AppShell>
  );
}
