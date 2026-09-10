import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { checkStatus } from "../api";
import { Clock, CheckCircle2 } from "lucide-react";

export default function PendingPage() {
  const navigate = useNavigate();
  const { user, token, login } = useAuth();

  // If there's no user or they aren't pending, redirect
  useEffect(() => {
    if (!token) {
      navigate("/login", { replace: true });
      return;
    }
    if (user?.approvalStatus !== "PENDING") {
      navigate("/dashboard", { replace: true });
    }
  }, [token, user?.approvalStatus, navigate]);

  // Poll for status updates
  useEffect(() => {
    if (!token || user?.approvalStatus !== "PENDING") return;

    const intervalId = setInterval(async () => {
      try {
        const res = await checkStatus();
        const { token: newToken, user: newUser } = res.data;
        if (newUser.approvalStatus === "APPROVED") {
          login(newToken, newUser);
          window.location.href = "/dashboard";
        } else if (newUser.approvalStatus === "REJECTED") {
          login(newToken, newUser);
          window.location.href = "/login?rejected=true";
        }
      } catch (err) {
        console.error("Status check failed:", err);
      }
    }, 5000); // Check every 5 seconds

    return () => clearInterval(intervalId);
  }, [token, user?.approvalStatus, login, navigate]);

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
      <div className="pointer-events-none fixed inset-x-0 top-0 h-64 bg-[radial-gradient(60%_100%_at_50%_0%,var(--color-primary-soft),transparent_70%)] opacity-25 dark:opacity-15" />

      <div className="relative w-full max-w-md surface-lift rounded-3xl p-8 border border-border text-center">
        <div className="w-20 h-20 mx-auto mb-6 rounded-full bg-amber-500/10 flex items-center justify-center">
          <Clock className="h-10 w-10 text-amber-500 animate-pulse" />
        </div>

        <h1 className="font-display text-2xl font-semibold tracking-tight text-foreground mb-2">
          Approval Pending
        </h1>
        <p className="text-muted-foreground text-sm leading-relaxed mb-6">
          Your request to join the Mandal has been sent to the Admin.
          <br />
          You will get access automatically once they approve your request.
        </p>

        {user && (
          <div className="rounded-2xl bg-muted/30 p-4 text-sm text-muted-foreground">
            <div className="flex items-center gap-2 justify-center">
              <CheckCircle2 className="h-4 w-4 text-green-500" />
              <span>Name: <strong className="text-foreground">{user.name}</strong></span>
            </div>
            {user.phone && (
              <div className="flex items-center gap-2 justify-center mt-1">
                <CheckCircle2 className="h-4 w-4 text-green-500" />
                <span>Phone: <strong className="text-foreground">{user.phone}</strong></span>
              </div>
            )}
          </div>
        )}

        <button
          onClick={async () => {
            try {
              const res = await checkStatus();
              const { token: newToken, user: newUser } = res.data;
              if (newUser.approvalStatus === "APPROVED") {
                login(newToken, newUser);
                window.location.href = "/dashboard";
              } else if (newUser.approvalStatus === "REJECTED") {
                login(newToken, newUser);
                window.location.href = "/login?rejected=true";
              } else {
                alert("Still pending! The admin hasn't approved you yet, or the database hasn't updated.");
              }
            } catch (err) {
              alert("Error checking status: " + err.message);
            }
          }}
          className="mt-6 flex min-h-[44px] w-full items-center justify-center rounded-xl bg-secondary text-sm font-semibold text-foreground transition-colors hover:bg-secondary/80"
        >
          Check Status Now
        </button>

        <p className="text-xs text-muted-foreground mt-4">
          Please wait... this page will automatically refresh when you are approved.
        </p>
      </div>
    </div>
  );
}
