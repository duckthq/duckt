# Mainframe server

## Development

1. Install Leiningen: https://leiningen.org/#docs

2. Install a PostgreSQL with a database named `mainframe` and a user `mainframe` with password `mainframe`
```sh
# create the user. For development in your local machine, this is ok:
psql -c "CREATE USER mainframe WITH SUPERUSER PASSWORD 'mainframe';"
# create the database
psql -c "CREATE DATABASE mainframe WITH ENCODING='UTF8' OWNER=mainframe;"
# make sure your db is in UTC
psql -c "ALTER DATABASE mainframe SET timezone TO 'UTC';"
```

3. Copy the `.env.sample` file to `.env`, it's ready to use with default values, but you can change it if you want.

4. Run the server with `lein run`

## Migrations

Migrations are auto applied on server startup. To rollback, you can use any isntruction in the migration lib (CLI or through code in the REPL): [https://github.com/igrishaev/pg2/blob/master/docs/migrations.md#api-interface](https://github.com/igrishaev/pg2/blob/master/docs/migrations.md#api-interface)


