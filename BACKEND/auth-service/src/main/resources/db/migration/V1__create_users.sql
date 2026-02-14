CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    roles TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- Insert dev user with a hardcoded UUID
INSERT INTO users (id, email, password_hash, roles)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'admin@minierp.com',
           '$2b$10$lYIMjsBYsTnyzRA18SQmd.8HIgXK4zOxMJ18fDs11V5OuVsFGBjkm',
           'OWNER'
       )
    ON CONFLICT (email) DO NOTHING;
