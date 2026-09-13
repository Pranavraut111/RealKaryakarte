const { Pool } = require('pg');
const pool = new Pool({
  connectionString: 'postgresql://neondb_owner:npg_u1vQyBfSgI2l@ep-crimson-water-a17p6wxy-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require'
});

async function run() {
  const res = await pool.query("SELECT id, name, phone, approval_status FROM users WHERE name = 'test2'");
  console.log(res.rows);
  pool.end();
}
run();
