# --- !Ups

CREATE EXTENSION IF NOT EXISTS "citext";

CREATE SCHEMA auth;

CREATE TABLE auth.user (
  id                UUID    NOT NULL PRIMARY KEY,
  handle            CITEXT  NOT NULL,
  domain            VARCHAR,
  name              VARCHAR,
  faith_tradition   VARCHAR,
  email             VARCHAR,
  avatar_url        VARCHAR,
  profile           TEXT,
  signed_up_at      TIMESTAMPTZ DEFAULT NOW(),
  activated         BOOLEAN NOT NULL,
  public_key        TEXT NOT NULL,
  private_key       TEXT,
  salt              BYTEA,
  CONSTRAINT auth_user_unique_handle_domain UNIQUE (handle, domain),
  CONSTRAINT auth_user_unique_email UNIQUE (email)
);

CREATE TABLE auth.login_info (
  id           BIGSERIAL NOT NULL PRIMARY KEY,
  provider_id  VARCHAR NOT NULL,
  provider_key VARCHAR NOT NULL
);

CREATE INDEX auth_login_info_key on auth.login_info(provider_id, provider_key);

CREATE TABLE auth.user_login_info (
  user_id       UUID   NOT NULL,
  login_info_id BIGINT NOT NULL,
  CONSTRAINT auth_user_login_info_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id),
  CONSTRAINT auth_user_login_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id),
  CONSTRAINT auth_user_login_info_pk PRIMARY KEY (user_id, login_info_id)
);

CREATE TABLE auth.google_totp_info (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  login_info_id BIGINT NOT NULL,
  shared_key    VARCHAR NOT NULL,
  CONSTRAINT auth_google_totp_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id)
);

CREATE TABLE auth.totp_scratch_code (
  id                    BIGSERIAL NOT NULL PRIMARY KEY,
  totp_google_info_id   BIGINT NOT NULL,
  hasher                VARCHAR NOT NULL,
  password              VARCHAR NOT NULL,
  salt                  VARCHAR,
  CONSTRAINT auth_totp_scratch_code_google_totp_info_id_fk FOREIGN KEY (totp_google_info_id) REFERENCES auth.google_totp_info (id)
);

CREATE TABLE auth.oauth2_info (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  access_token  VARCHAR   NOT NULL,
  token_type    VARCHAR,
  expires_in    INT,
  refresh_token VARCHAR,
  login_info_id BIGINT    NOT NULL,
  CONSTRAINT auth_oauth2_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id)
);

CREATE TABLE auth.password_info (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  hasher        VARCHAR NOT NULL,
  password      VARCHAR NOT NULL,
  salt          VARCHAR,
  login_info_id BIGINT  NOT NULL,
  CONSTRAINT auth_password_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id)
);

CREATE TABLE auth.token (
  id      UUID        NOT NULL PRIMARY KEY,
  user_id UUID        NOT NULL,
  expiry  TIMESTAMPTZ NOT NULL,
  CONSTRAINT auth_token_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE SCHEMA app;

CREATE TABLE app.prayer_request (
  id            UUID        NOT NULL PRIMARY KEY,
  user_id       UUID        NOT NULL,
  request       TEXT        NOT NULL,
  when_created  TIMESTAMPTZ NOT NULL,
  is_anonymous  BOOLEAN     NOT NULL,
  visibility    VARCHAR     NOT NULL,
  search_field  tsvector,
  CONSTRAINT app_prayer_request_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE FUNCTION app.update_prayer_request_search_field() RETURNS TRIGGER AS $$
BEGIN
   NEW.search_field := to_tsvector(NEW.request);;
   RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_update_prayer_request_search_field
BEFORE INSERT OR UPDATE ON app.prayer_request
FOR EACH ROW EXECUTE FUNCTION app.update_prayer_request_search_field();

CREATE INDEX prayer_request_search_idx ON app.prayer_request USING GIN(search_field);

CREATE TABLE app.prayer_request_reaction (
  id            UUID        NOT NULL PRIMARY KEY,
  request_id    UUID        NOT NULL,
  user_id       UUID        NOT NULL,
  reaction_type VARCHAR     NOT NULL,
  CONSTRAINT app_prayer_request_reaction_request_id_fk FOREIGN KEY (request_id) references app.prayer_request (id) ON DELETE CASCADE,
  CONSTRAINT app_prayer_request_reaction_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id),
  CONSTRAINT unique_user_reaction_per_request UNIQUE (request_id, user_id)
);

CREATE TABLE app.prayer_response (
  id            UUID        NOT NULL PRIMARY KEY,
  request_id    UUID        NOT NULL,
  user_id       UUID        NOT NULL,
  response      TEXT        NOT NULL,
  search_field  tsvector,
  when_created  TIMESTAMPTZ NOT NULL,
  CONSTRAINT app_prayer_response_request_id_fk FOREIGN KEY (request_id) REFERENCES app.prayer_request (id) ON DELETE CASCADE,
  CONSTRAINT app_prayer_response_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE FUNCTION app.update_prayer_response_search_field() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_field := to_tsvector(NEW.response);;
    RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_update_prayer_response_search_field
BEFORE INSERT OR UPDATE ON app.prayer_response
FOR EACH ROW EXECUTE FUNCTION app.update_prayer_response_search_field();

CREATE INDEX prayer_response_search_idx ON app.prayer_response USING GIN(search_field);

CREATE TABLE app.prayer_response_reaction (
  id            UUID        NOT NULL PRIMARY KEY,
  response_id   UUID        NOT NULL,
  user_id       UUID        NOT NULL,
  reaction_type VARCHAR     NOT NULL,
  CONSTRAINT app_prayer_response_reaction_response_id_fk FOREIGN KEY (response_id) REFERENCES app.prayer_response (id) ON DELETE CASCADE,
  CONSTRAINT app_prayer_response_reaction_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id),
  CONSTRAINT unique_user_reaction_per_response UNIQUE (response_id, user_id)
);

CREATE TABLE app.prayer_group (
  id            UUID            NOT NULL PRIMARY KEY,
  name          VARCHAR(255)    NOT NULL,
  handle        CITEXT          NOT NULL,
  domain        VARCHAR,
  description   TEXT            NOT NULL,
  search_field  tsvector,
  when_created  TIMESTAMPTZ     NOT NULL,
  who_created   UUID            NOT NULL,
  public_key    TEXT            NOT NULL,
  private_key   TEXT,
  salt          BYTEA,
  CONSTRAINT app_prayer_group_handle_domain UNIQUE (handle, domain),
  CONSTRAINT app_prayer_group_who_created FOREIGN KEY (who_created) REFERENCES auth.user (id)
);

CREATE FUNCTION app.update_prayer_group_search_field() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_field := to_tsvector(NEW.name || ' ' || NEW.description);;
    RETURN NEW;;
END;;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_update_prayer_group_search_field
BEFORE INSERT OR UPDATE ON app.prayer_group
FOR EACH ROW EXECUTE FUNCTION app.update_prayer_group_search_field();

CREATE INDEX prayer_group_search_idx ON app.prayer_group USING GIN(search_field);

CREATE TABLE app.prayer_group_membership (
  id            UUID        NOT NULL PRIMARY KEY,
  group_id      UUID        NOT NULL,
  user_id       UUID        NOT NULL,
  when_created  TIMESTAMPTZ NOT NULL,
  CONSTRAINT app_prayer_group_membership_group_id FOREIGN KEY (group_id) REFERENCES app.prayer_group (id),
  CONSTRAINT app_prayer_group_membership_user_id FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE TABLE app.prayer_request_group (
  request_id    UUID        NOT NULL,
  group_id      UUID        NOT NULL,
  PRIMARY KEY (request_id, group_id),
  CONSTRAINT app_prayer_request_group_request_id FOREIGN KEY (request_id) REFERENCES app.prayer_request (id) ON DELETE CASCADE,
  CONSTRAINT app_prayer_request_group_group_id FOREIGN KEY (group_id) REFERENCES app.prayer_group (id)
);

CREATE TABLE app.handle (
  handle        CITEXT          NOT NULL,
  domain        VARCHAR,
  entity_type   VARCHAR         NOT NULL,
  entity_id     UUID            NOT NULL,
  when_created  TIMESTAMPTZ     NOT NULL,
  PRIMARY KEY (entity_type, entity_id),
  CONSTRAINT app_handle_unique_handle_domain UNIQUE (handle, domain)
);

CREATE TABLE app.prayer_request_mention (
  id                    UUID        NOT NULL PRIMARY KEY,
  request_id            UUID        NOT NULL,
  handle                CITEXT      NOT NULL,
  domain                VARCHAR,
  when_created          TIMESTAMPTZ NOT NULL,
  CONSTRAINT app_prayer_request_mention_request_id_fk FOREIGN KEY (request_id) REFERENCES app.prayer_request (id) ON DELETE CASCADE,
  CONSTRAINT app_prayer_request_mention_handle_domain_fk FOREIGN KEY (handle, domain) REFERENCES app.handle (handle, domain)
);

CREATE TABLE app.prayer_response_mention (
  id                    UUID        NOT NULL PRIMARY KEY,
  response_id           UUID        NOT NULL,
  handle                CITEXT      NOT NULL,
  domain                VARCHAR,
  when_created          TIMESTAMPTZ NOT NULL,
  CONSTRAINT app_prayer_response_mention_response_id_fk FOREIGN KEY (response_id) REFERENCES app.prayer_response (id) ON DELETE CASCADE,
  CONSTRAINT app_prayer_response_mention_handle_domain_fk FOREIGN KEY (handle, domain) REFERENCES app.handle (handle, domain)
);

CREATE TABLE app.follow (
  user_id           UUID              NOT NULL,
  target_user_id    UUID              NOT NULL,
  when_created      TIMESTAMPTZ       NOT NULL,
  PRIMARY KEY (user_id, target_user_id),
  CONSTRAINT app_follow_user_id FOREIGN KEY (user_id) REFERENCES auth.user (id),
  CONSTRAINT app_follow_target_user_id FOREIGN KEY (target_user_id) REFERENCES auth.user (id)
);

#--- !Downs

DROP TABLE app.follow;
DROP TABLE app.prayer_response_mention;
DROP TABLE app.prayer_request_mention;
DROP TABLE app.handle;
DROP TABLE app.prayer_request_group;
DROP TABLE app.prayer_group_membership;
DROP TABLE app.prayer_group;
DROP FUNCTION app.update_prayer_group_search_field;
DROP TABLE app.prayer_response_reaction;
DROP TABLE app.prayer_response;
DROP FUNCTION app.update_prayer_response_search_field;
DROP TABLE app.prayer_request_reaction;
DROP TABLE app.prayer_request;
DROP FUNCTION app.update_prayer_request_search_field;
DROP SCHEMA app;

DROP TABLE auth.token;
DROP TABLE auth.password_info;
DROP TABLE auth.oauth2_info;
DROP TABLE auth.totp_scratch_code;
DROP TABLE auth.google_totp_info;
DROP TABLE auth.user_login_info;
DROP TABLE auth.login_info;
DROP TABLE auth.user;
DROP SCHEMA auth;