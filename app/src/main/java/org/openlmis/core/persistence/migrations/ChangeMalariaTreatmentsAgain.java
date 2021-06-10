package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class ChangeMalariaTreatmentsAgain extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE treatment RENAME TO treatment2;");
    execSQL("ALTER TABLE implementation RENAME TO implementation2;");
    execSQL("ALTER TABLE malaria_program RENAME TO malaria_program2;");

    execSQL("CREATE TABLE `malaria_program` " +
        "( `id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`reportedDate` VARCHAR NOT NULL, " +
        "`startPeriodDate` VARCHAR NOT NULL, " +
        "`endPeriodDate` VARCHAR NOT NULL, " +
        "`status` INTEGER NOT NULL, " +
        "`username` VARCHAR NOT NULL, " +
        "`createdAt` VARCHAR NOT NULL, " +
        "`updatedAt` VARCHAR NOT NULL);");
    execSQL("CREATE TABLE `implementation` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "`program` INTEGER NOT NULL, " +
        "`executor` VARCHAR NOT NULL, " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL, " +
        "FOREIGN KEY (program) " +
        "REFERENCES malaria_program(id));");
    execSQL("CREATE TABLE `treatment` " +
        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
        "`implementation` INTEGER NOT NULL," +
        "`product` INTEGER NOT NULL, " +
        "`amount` INTEGER NOT NULL, " +
        "`stock` INTEGER NOT NULL, " +
        "`createdAt` VARCHAR NOT NULL , " +
        "`updatedAt` VARCHAR NOT NULL, " +
        "FOREIGN KEY (product) REFERENCES product(id)," +
        " FOREIGN KEY (implementation) REFERENCES implementation(id));");

    execSQL("INSERT OR IGNORE INTO malaria_program " +
        "(id, reportedDate, startPeriodDate, endPeriodDate, status, username, createdAt,updatedAt) "
        +
        "SELECT id, reportedDate, startPeriodDate, endPeriodDate, status, username, createdAt,updatedAt from malaria_program2 ;");
    execSQL("DROP TABLE `malaria_program2`;");

    execSQL("INSERT OR IGNORE INTO implementation " +
        "(id, program, executor, createdAt, updatedAt) " +
        "SELECT id, program, executor, createdAt, updatedAt from implementation2 ;");
    execSQL("DROP TABLE `implementation2`;");

    execSQL("INSERT OR IGNORE INTO treatment " +
        "(id, implementation, product, amount, stock, createdAt, updatedAt) " +
        "SELECT id, implementation, product, amount, stock, createdAt, updatedAt from treatment2 ;");
    execSQL("DROP TABLE `treatment2`;");
  }
}
