CREATE DATABASE IF NOT EXISTS clanky_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS clanky_catalogo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS clanky_carrito CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON clanky_usuarios.* TO 'clanky'@'%';
GRANT ALL PRIVILEGES ON clanky_catalogo.* TO 'clanky'@'%';
GRANT ALL PRIVILEGES ON clanky_carrito.* TO 'clanky'@'%';

FLUSH PRIVILEGES;
