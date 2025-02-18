-- set the database to UTC
SET timezone TO 'UTC';

create table IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255),
  given_name VARCHAR(255),
  family_name VARCHAR(255),
  fullname VARCHAR(255),
  preferred_username VARCHAR(255),
  profile TEXT,
  picture TEXT,
  birthdate DATE,
  zoneinfo VARCHAR(50),
  locale VARCHAR(10),
  email_verified BOOLEAN DEFAULT FALSE,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

create table IF NOT EXISTS user_preferences (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  selected_workspace UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

create table IF NOT EXISTS workspaces (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

create table IF NOT EXISTS user_workspaces (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  role VARCHAR(50) NOT NULL, -- e.g., 'admin', 'member', 'guest'
  joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, workspace_id)
);

CREATE TABLE IF NOT EXISTS proxies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  proxy_key_hash TEXT NOT NULL,
  name VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL, -- e.g., 'online', 'offline'
  description TEXT,
  target_url TEXT NOT NULL,
  host_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- register customers from the jwt token
create table if not exists customers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sub TEXT NOT NULL,
  metadata JSONB,
  hit_count INTEGER DEFAULT 1,
  last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  -- TODO: make this NOT NULL
  workspace_id UUID,
  joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(sub, workspace_id)
);

CREATE TABLE IF NOT EXISTS endpoints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  proxy_id UUID NOT NULL REFERENCES proxies(id),
  path TEXT NOT NULL,
  method VARCHAR(10) NOT NULL,
  last_used_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  workspace_id UUID NOT NULL,
  hit_count INTEGER DEFAULT 1,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(proxy_id, path, method)
);

-- TODO: relate request to events somehow as events is more analytics friendly
-- maybe add some rules that when a request meets certain criteria, it will be
-- logged as an event as well
create table IF NOT EXISTS requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type VARCHAR(50) NOT NULL, -- e.g., 'proxy', 'manual'
  url TEXT,
  uri TEXT,
  endpoint_id UUID NOT NULL REFERENCES endpoints(id),
  -- add proxy_id to easily find it
  -- maybe reference it as well
  proxy_id UUID,
  query_params JSONB,
  status_code INTEGER,
  request_headers JSONB,
  response_headers JSONB,
  method VARCHAR(10),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  response_time TIMESTAMP WITH TIME ZONE,
  elapsed_time BIGINT,
  workspace_id UUID NOT NULL,
  customer_id UUID,
  customer_sub TEXT,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX requests_created_at_brin
  ON requests USING BRIN (created_at) WITH (pages_per_range = 512);

CREATE TABLE IF NOT EXISTS customer_stats (
  customer_id UUID NOT NULL REFERENCES customers(id),
  total_requests BIGINT DEFAULT 1,
  endpoint_counts JSONB DEFAULT '{}'::jsonb
);
