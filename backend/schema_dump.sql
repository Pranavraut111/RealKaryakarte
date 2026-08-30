--
-- PostgreSQL database dump
--

\restrict WBgnmWQdIVxLt8AwAXErLRu4qvOZxcr0U94JlaCyQUDKkBb4KxQxoWQv068FUH1

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: broadcasts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.broadcasts (
    id bigint NOT NULL,
    message_text text NOT NULL,
    channel character varying(10),
    sent_by bigint,
    recipient_group character varying(20),
    sent_at timestamp without time zone DEFAULT now(),
    status character varying(20) DEFAULT 'SENT'::character varying,
    CONSTRAINT broadcasts_channel_check CHECK (((channel)::text = ANY ((ARRAY['SMS'::character varying, 'WHATSAPP'::character varying])::text[])))
);


ALTER TABLE public.broadcasts OWNER TO postgres;

--
-- Name: broadcasts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.broadcasts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.broadcasts_id_seq OWNER TO postgres;

--
-- Name: broadcasts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.broadcasts_id_seq OWNED BY public.broadcasts.id;


--
-- Name: contributions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.contributions (
    id bigint NOT NULL,
    member_id bigint,
    member_name character varying(150),
    amount numeric(10,2) NOT NULL,
    payment_method character varying(20),
    collected_by bigint,
    receipt_no character varying(30) NOT NULL,
    receipt_pdf_url text,
    note text,
    contribution_date date NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    created_by bigint,
    CONSTRAINT contributions_amount_check CHECK ((amount > (0)::numeric)),
    CONSTRAINT contributions_payment_method_check CHECK (((payment_method)::text = ANY ((ARRAY['CASH'::character varying, 'UPI'::character varying, 'BANK_TRANSFER'::character varying, 'CHEQUE'::character varying])::text[])))
);


ALTER TABLE public.contributions OWNER TO postgres;

--
-- Name: contributions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.contributions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.contributions_id_seq OWNER TO postgres;

--
-- Name: contributions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.contributions_id_seq OWNED BY public.contributions.id;


--
-- Name: expense_categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.expense_categories (
    id bigint NOT NULL,
    name_en character varying(100) NOT NULL,
    name_mr character varying(100)
);


ALTER TABLE public.expense_categories OWNER TO postgres;

--
-- Name: expense_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.expense_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.expense_categories_id_seq OWNER TO postgres;

--
-- Name: expense_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.expense_categories_id_seq OWNED BY public.expense_categories.id;


--
-- Name: expenses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.expenses (
    id bigint NOT NULL,
    item_name character varying(200) NOT NULL,
    category_id bigint,
    amount numeric(10,2) NOT NULL,
    purchased_by bigint,
    vendor_name character varying(150),
    item_photo_url text,
    receipt_photo_url text,
    approval_status character varying(20) DEFAULT 'APPROVED'::character varying,
    approved_by bigint,
    expense_date date NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    created_by bigint,
    CONSTRAINT expenses_amount_check CHECK ((amount > (0)::numeric)),
    CONSTRAINT expenses_approval_status_check CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.expenses OWNER TO postgres;

--
-- Name: expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.expenses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.expenses_id_seq OWNER TO postgres;

--
-- Name: expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.expenses_id_seq OWNED BY public.expenses.id;


--
-- Name: mandal_settings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mandal_settings (
    id bigint NOT NULL,
    mandal_name character varying(200),
    event_year integer,
    logo_url text,
    suggested_vargani_amount numeric(10,2),
    expense_approval_required boolean DEFAULT false,
    karyakarta_can_broadcast boolean DEFAULT true,
    sms_api_key text,
    whatsapp_api_key text
);


ALTER TABLE public.mandal_settings OWNER TO postgres;

--
-- Name: mandal_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mandal_settings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mandal_settings_id_seq OWNER TO postgres;

--
-- Name: mandal_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mandal_settings_id_seq OWNED BY public.mandal_settings.id;


--
-- Name: notices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notices (
    id bigint NOT NULL,
    title character varying(200) NOT NULL,
    body text NOT NULL,
    photo_url text,
    posted_by bigint,
    is_pinned boolean DEFAULT false,
    publish_at timestamp without time zone DEFAULT now(),
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE public.notices OWNER TO postgres;

--
-- Name: notices_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notices_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notices_id_seq OWNER TO postgres;

--
-- Name: notices_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notices_id_seq OWNED BY public.notices.id;


--
-- Name: otp_store; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.otp_store (
    id bigint NOT NULL,
    phone character varying(15) NOT NULL,
    otp_code character varying(6) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    verified boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT now()
);


ALTER TABLE public.otp_store OWNER TO postgres;

--
-- Name: otp_store_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.otp_store_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.otp_store_id_seq OWNER TO postgres;

--
-- Name: otp_store_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.otp_store_id_seq OWNED BY public.otp_store.id;


--
-- Name: receipt_no_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.receipt_no_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.receipt_no_seq OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    name character varying(150) NOT NULL,
    phone character varying(15) NOT NULL,
    email character varying(150),
    role character varying(20) DEFAULT 'MEMBER'::character varying NOT NULL,
    language_pref character varying(5) DEFAULT 'en'::character varying,
    photo_url text,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'KARYAKARTA'::character varying, 'MEMBER'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: broadcasts id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.broadcasts ALTER COLUMN id SET DEFAULT nextval('public.broadcasts_id_seq'::regclass);


--
-- Name: contributions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contributions ALTER COLUMN id SET DEFAULT nextval('public.contributions_id_seq'::regclass);


--
-- Name: expense_categories id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expense_categories ALTER COLUMN id SET DEFAULT nextval('public.expense_categories_id_seq'::regclass);


--
-- Name: expenses id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses ALTER COLUMN id SET DEFAULT nextval('public.expenses_id_seq'::regclass);


--
-- Name: mandal_settings id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mandal_settings ALTER COLUMN id SET DEFAULT nextval('public.mandal_settings_id_seq'::regclass);


--
-- Name: notices id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notices ALTER COLUMN id SET DEFAULT nextval('public.notices_id_seq'::regclass);


--
-- Name: otp_store id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.otp_store ALTER COLUMN id SET DEFAULT nextval('public.otp_store_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: broadcasts broadcasts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.broadcasts
    ADD CONSTRAINT broadcasts_pkey PRIMARY KEY (id);


--
-- Name: contributions contributions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contributions
    ADD CONSTRAINT contributions_pkey PRIMARY KEY (id);


--
-- Name: contributions contributions_receipt_no_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contributions
    ADD CONSTRAINT contributions_receipt_no_key UNIQUE (receipt_no);


--
-- Name: expense_categories expense_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expense_categories
    ADD CONSTRAINT expense_categories_pkey PRIMARY KEY (id);


--
-- Name: expenses expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_pkey PRIMARY KEY (id);


--
-- Name: mandal_settings mandal_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mandal_settings
    ADD CONSTRAINT mandal_settings_pkey PRIMARY KEY (id);


--
-- Name: notices notices_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notices
    ADD CONSTRAINT notices_pkey PRIMARY KEY (id);


--
-- Name: otp_store otp_store_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.otp_store
    ADD CONSTRAINT otp_store_pkey PRIMARY KEY (id);


--
-- Name: users users_phone_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_key UNIQUE (phone);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_contributions_collector; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contributions_collector ON public.contributions USING btree (collected_by);


--
-- Name: idx_contributions_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contributions_date ON public.contributions USING btree (contribution_date);


--
-- Name: idx_contributions_member; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contributions_member ON public.contributions USING btree (member_id);


--
-- Name: idx_contributions_method; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contributions_method ON public.contributions USING btree (payment_method);


--
-- Name: idx_expenses_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_expenses_category ON public.expenses USING btree (category_id);


--
-- Name: idx_expenses_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_expenses_date ON public.expenses USING btree (expense_date);


--
-- Name: idx_expenses_purchaser; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_expenses_purchaser ON public.expenses USING btree (purchased_by);


--
-- Name: idx_expenses_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_expenses_status ON public.expenses USING btree (approval_status);


--
-- Name: idx_notices_pinned; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notices_pinned ON public.notices USING btree (is_pinned);


--
-- Name: idx_notices_published; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notices_published ON public.notices USING btree (publish_at);


--
-- Name: idx_otp_phone; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_otp_phone ON public.otp_store USING btree (phone);


--
-- Name: idx_users_phone; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_phone ON public.users USING btree (phone);


--
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_role ON public.users USING btree (role);


--
-- Name: broadcasts broadcasts_sent_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.broadcasts
    ADD CONSTRAINT broadcasts_sent_by_fkey FOREIGN KEY (sent_by) REFERENCES public.users(id);


--
-- Name: contributions contributions_collected_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contributions
    ADD CONSTRAINT contributions_collected_by_fkey FOREIGN KEY (collected_by) REFERENCES public.users(id);


--
-- Name: contributions contributions_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contributions
    ADD CONSTRAINT contributions_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: contributions contributions_member_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contributions
    ADD CONSTRAINT contributions_member_id_fkey FOREIGN KEY (member_id) REFERENCES public.users(id);


--
-- Name: expenses expenses_approved_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.users(id);


--
-- Name: expenses expenses_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.expense_categories(id);


--
-- Name: expenses expenses_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: expenses expenses_purchased_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_purchased_by_fkey FOREIGN KEY (purchased_by) REFERENCES public.users(id);


--
-- Name: notices notices_posted_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notices
    ADD CONSTRAINT notices_posted_by_fkey FOREIGN KEY (posted_by) REFERENCES public.users(id);


--
-- Name: TABLE broadcasts; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.broadcasts TO mandal_app;


--
-- Name: SEQUENCE broadcasts_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.broadcasts_id_seq TO mandal_app;


--
-- Name: TABLE contributions; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.contributions TO mandal_app;


--
-- Name: SEQUENCE contributions_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.contributions_id_seq TO mandal_app;


--
-- Name: TABLE expense_categories; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.expense_categories TO mandal_app;


--
-- Name: SEQUENCE expense_categories_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.expense_categories_id_seq TO mandal_app;


--
-- Name: TABLE expenses; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.expenses TO mandal_app;


--
-- Name: SEQUENCE expenses_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.expenses_id_seq TO mandal_app;


--
-- Name: TABLE mandal_settings; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.mandal_settings TO mandal_app;


--
-- Name: SEQUENCE mandal_settings_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.mandal_settings_id_seq TO mandal_app;


--
-- Name: TABLE notices; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.notices TO mandal_app;


--
-- Name: SEQUENCE notices_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.notices_id_seq TO mandal_app;


--
-- Name: TABLE otp_store; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.otp_store TO mandal_app;


--
-- Name: SEQUENCE otp_store_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.otp_store_id_seq TO mandal_app;


--
-- Name: SEQUENCE receipt_no_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.receipt_no_seq TO mandal_app;


--
-- Name: TABLE users; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON TABLE public.users TO mandal_app;


--
-- Name: SEQUENCE users_id_seq; Type: ACL; Schema: public; Owner: postgres
--

GRANT ALL ON SEQUENCE public.users_id_seq TO mandal_app;


--
-- PostgreSQL database dump complete
--

\unrestrict WBgnmWQdIVxLt8AwAXErLRu4qvOZxcr0U94JlaCyQUDKkBb4KxQxoWQv068FUH1

