# Duckt Server

## Development

1. Install Clojure: https://clojure.org/guides/install_clojure

2. Install a PostgreSQL with a database named `duckt` and a user `duckt` with password `duckt`
```sh
# create the user. For development in your local machine, this is ok:
psql -c "CREATE USER duckt WITH SUPERUSER PASSWORD 'duckt';"
# create the database
psql -c "CREATE DATABASE duckt WITH ENCODING='UTF8' OWNER=duckt;"
# make sure your db is in UTC
psql -c "ALTER DATABASE duckt SET timezone TO 'UTC';"
```

3. Copy the `.env.sample` file to `.env`, it's ready to use with default values, but you can change it if you want.

4. Run the REPL with `clojure -M:dev`

    4.1. From inside the REPL, import the dev namespace and start the server:
    ```clojure
    (require 'dev)
    (dev/start)
    ; option to restart
    (dev/restart)
    ```

## Migrations

Migrations are auto applied on server startup. To rollback, you can use any isntruction in the migration lib (CLI or through code in the REPL): [https://github.com/igrishaev/pg2/blob/master/docs/migrations.md#api-interface](https://github.com/igrishaev/pg2/blob/master/docs/migrations.md#api-interface)


