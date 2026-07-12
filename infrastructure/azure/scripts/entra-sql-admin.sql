-- Microsoft Entra ID database admin setup for Azure SQL Database
-- Connect to the 'digitalbanking' database as the Entra ID SQL admin, then run:

-- 1. Create the user mapping from Microsoft Entra ID
CREATE USER [user@yourdomain.com] FROM EXTERNAL PROVIDER;

-- 2. Grant them full database admin and query access
ALTER ROLE db_owner ADD MEMBER [user@yourdomain.com];
