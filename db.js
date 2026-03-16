const { Pool } = require('pg'); // Importa el driver de PostgreSQL

const pool = new Pool({
  connectionString: process.env.DATABASE_URL, // Lee el secreto de GitHub/Vercel
  ssl: {
    rejectUnauthorized: false // Requerido por Neon para conexiones seguras
  }
});
