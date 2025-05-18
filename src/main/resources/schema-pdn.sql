CREATE TABLE IF NOT EXISTS public.users
(
    id bigint NOT NULL,
    fullname character varying(255) COLLATE pg_catalog."default" NOT NULL,
    is_retired boolean NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    password_change_required boolean NOT NULL,
    username character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.role
(
    id bigint NOT NULL,
    role character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT role_pkey PRIMARY KEY (id),
    CONSTRAINT ukbjxn5ii7v7ygwx39et0wawu0q UNIQUE (role)
);

CREATE TABLE IF NOT EXISTS public.user_role
(
    id bigint NOT NULL,
    role_id bigint,
    user_id bigint,
    CONSTRAINT user_role_pkey PRIMARY KEY (id),
    CONSTRAINT fka68196081fvovjhkek5m97n3y FOREIGN KEY (role_id)
        REFERENCES public.role (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fkj345gk1bovqvfame88rcx7yyx FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE SEQUENCE IF NOT EXISTS public.role_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
	
	
CREATE SEQUENCE IF NOT EXISTS public.users_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
	
CREATE SEQUENCE IF NOT EXISTS public.user_role_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;