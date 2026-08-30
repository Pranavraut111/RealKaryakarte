import { Link } from "react-router-dom";
import { ArrowRight, FileText, CheckCircle2, Megaphone } from "lucide-react";
import { FlameIcon } from "@/components/AppShell";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-background text-foreground selection:bg-primary/20">
      {/* Navbar */}
      <nav className="fixed top-0 z-50 w-full glass border-b border-border/40">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <img src="/logo.png" alt="Ganpati Logo" className="h-10 w-10 object-contain" />
            <span className="font-display text-xl font-bold tracking-tight text-foreground">Mandal Ledger</span>
          </div>
          <div className="flex items-center gap-3 sm:gap-5">
            <Link
              to="/login"
              className="rounded-full bg-primary px-4 py-1.5 sm:px-5 sm:py-2 text-xs sm:text-sm font-semibold text-primary-foreground shadow-sm transition-all hover:bg-primary-deep active:scale-95"
            >
              Login
            </Link>
            <div className="flex flex-col items-start text-[9px] sm:text-xs text-muted-foreground border-l border-border/50 pl-2 sm:pl-4">
              <span className="font-medium text-foreground/80 leading-tight">Developed by<br className="sm:hidden" /> Pranav Raut</span>
              <a href="mailto:praut1086@gmail.com" className="hover:text-primary transition-colors leading-tight">praut1086@gmail.com</a>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative flex min-h-[90vh] flex-col items-center justify-center overflow-hidden px-6 pt-32 pb-16 text-center">
        <div className="pointer-events-none absolute left-1/2 top-1/2 -z-10 h-[500px] w-[500px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-primary/20 blur-[120px]" />

        <div className="animate-in fade-in slide-in-from-bottom-8 duration-1000 fill-mode-both">
          <div className="mx-auto mb-8 h-48 w-40 overflow-hidden rounded-3xl shadow-2xl ring-2 ring-border">
            <img src="/logo2.png" alt="Ganpati Logo" className="h-full w-full object-cover" />
          </div>
          <h1 className="font-display text-5xl font-bold tracking-tight text-foreground sm:text-7xl">
            Transparent <br className="hidden sm:block" />
            <span className="text-primary">Mandal Accounts.</span>
          </h1>
          <p className="mx-auto mt-6 max-w-2xl text-lg leading-relaxed text-muted-foreground sm:text-xl">
            Every vargani collected, every rupee spent, and the live balance in hand visible to every member, straight from your phone.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link
              to="/login"
              className="group flex items-center gap-2 rounded-full bg-ink px-8 py-4 text-base font-medium text-ink-foreground shadow-lg transition-all hover:bg-ink/90 active:scale-95"
            >
              Enter Ledger
              <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
            </Link>
            <a
              href="#features"
              className="flex items-center gap-2 rounded-full bg-secondary px-8 py-4 text-base font-medium text-secondary-foreground transition-all hover:bg-secondary/80 active:scale-95"
            >
              Learn More
            </a>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="bg-card px-6 py-24 sm:py-32">
        <div className="mx-auto max-w-6xl">
          <div className="text-center">
            <h2 className="font-display text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
              Everything you need, nothing you don't.
            </h2>
            <p className="mt-4 text-lg text-muted-foreground">
              Designed specifically for Ganpati Mandals to maintain absolute financial transparency.
            </p>
          </div>

          <div className="mt-20 grid grid-cols-1 gap-12 sm:grid-cols-3">
            <FeatureCard
              icon={CheckCircle2}
              title="Transparent Vargani"
              description="Track every contribution with digital records. Instantly see who gave how much, and issue professional digital receipts."
              delay="delay-100"
            />
            <FeatureCard
              icon={FileText}
              title="Live Expense Tracking"
              description="Log expenses on the go. From pandal decorations to prasad, every rupee spent is accounted for in real-time."
              delay="delay-200"
            />
            <FeatureCard
              icon={Megaphone}
              title="Digital Notice Board"
              description="Keep all karyakartas and members updated with a centralized digital notice board for important mandal announcements."
              delay="delay-300"
            />
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border/50 bg-background py-12 text-center text-sm text-muted-foreground">
        <div className="flex items-center justify-center gap-2">
          <FlameIcon className="h-4 w-4 text-primary" />
          <p>© 2026 Mandal Ledger. Built with devotion.</p>
        </div>
      </footer>
    </div>
  );
}

function FeatureCard({ icon: Icon, title, description, delay }) {
  return (
    <div className={`flex flex-col items-center text-center animate-in fade-in slide-in-from-bottom-8 duration-700 fill-mode-both ${delay}`}>
      <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-primary">
        <Icon className="h-8 w-8" />
      </div>
      <h3 className="font-display text-xl font-semibold text-foreground">{title}</h3>
      <p className="mt-3 leading-relaxed text-muted-foreground">{description}</p>
    </div>
  );
}
