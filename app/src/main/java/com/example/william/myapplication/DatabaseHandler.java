import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "FriendListDatabase";

    // Contacts table name
    private static final String TABLE_CONTACTS = "Friends";

    // Contacts Table Columns names
    private static final String KEY_Friend = "friend";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_Friend + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }


    public void addFriend(Database database) {}

    // Getting single contact
    public Database getDatabase(int id) {}

    // Getting All Contacts
    public List<Contact> getAllContacts() {}

    // Getting contacts Count
    public int getContactsCount() {}
    // Updating single contact
    public int updateContact(Contact contact) {}

    // Deleting single contact
    public void deleteContact(Contact contact) {}

}