/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 * This is an In-Memory implementation of the AccountDAO interface. This is not a persistent storage. A HashMap is
 * used to store the account details temporarily in the memory.
 */
public class persistantStorageAccountDAO extends SQLiteOpenHelper implements AccountDAO {
    private final Map<String, Account> accounts;
    public static final String DATABASE_NAME = "130664P.db";
    public static final String ACCOUNTS_TABLE_NAME = "Account";
    public static final String ACCOUNTS_COLUMN_ACCOUNT_NO = "ACno";
    public static final String ACCOUNTS_COLUMN_BANK_NAME = "bankName";
    public static final String ACCOUNTS_COLUMN_ACCOUNT_OWNER_NAME = "ownerName";
    public static final String ACCOUNTS_COLUMN_AMOUNT = "amount";


    public persistantStorageAccountDAO() {
        super(context, DATABASE_NAME , null, 1) ;
        this.accounts = new HashMap<>();
    }

    @Override
    public List<String> getAccountNumbersList() {
       // return new ArrayList<>(accounts.keySet());
        List<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Account", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_ACCOUNT_NO)));
            res.moveToNext();
        }
        return array_list;


    }

    @Override
    public List<Account> getAccountsList() {
        //return new ArrayList<>(accounts.values());
        List<Account> array_list = new ArrayList<Account>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Account", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            Account account = new Account(res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_ACCOUNT_NO))
                    ,res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_BANK_NAME))
                    ,res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_ACCOUNT_OWNER_NAME))
                    ,res.getDouble(res.getColumnIndex(ACCOUNTS_COLUMN_AMOUNT)));
            array_list.add(account);
            res.moveToNext();
        }
        return array_list;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where accountNo="+accountNo+"", null );
        if (res != null) {
            return new Account(res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_ACCOUNT_NO))
                    ,res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_BANK_NAME))
                    ,res.getString(res.getColumnIndex(ACCOUNTS_COLUMN_ACCOUNT_OWNER_NAME))
                    ,res.getDouble(res.getColumnIndex(ACCOUNTS_COLUMN_AMOUNT)));
        }
        String msg = "Account " + accountNo + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    @Override
    public void addAccount(Account account) {
       // accounts.put(account.getAccountNo(), account);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Acno", account.getAccountNo());
        contentValues.put("bankName", account.getBankName());
        contentValues.put("ownerName", account.getAccountHolderName());
        contentValues.put("amount", account.getBalance());
        db.insert("Account", null, contentValues);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Account", "Acno = ? ", new String[]{accountNo});
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Account account = getAccount(accountNo);

        switch (expenseType) {
            case EXPENSE:

                contentValues.put("amount", account.getBalance() - amount);
                break;
            case INCOME:

                contentValues.put("amount", account.getBalance() + amount);
                break;
        }
        db.update("Account", contentValues, "Acno = ? ", new String[] { accountNo } );
}
