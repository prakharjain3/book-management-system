- Created tables:
    - `T_GENRE` which stores the name of the genre and the corresponding id. New genres can be added by modifying the initial state of `T_GENRE` or by adding them through the `GenreDao`. The corresponding entity is `Genre`. 9 default values of genres are inserted into the table at startup, which can be seen in `books-core/src/main/resources/db/update/dbupdate-000-0.sql`. Schema:
    ```sql
    create memory table T_GENRE ( GNR_ID_C varchar(36) not null, GNR_NAME_C varchar(255) not null, primary key (GNR_ID_C) );
    ```
    - `T_BOOK_GENRE` is used because a book can have multiple genres but sql cannot store lists. We use a many to many mapping between a book's set of genres and the genre table using the javax persistence api. There is no corresponding entity class. Schema:
    ```sql
    create memory table T_BOOK_GENRE ( BOK_ID_C varchar(36) not null, GNR_ID_C varchar(36) not null, CONSTRAINT PK_BOOK_GENRE PRIMARY KEY (BOK_ID_C, GNR_ID_C), CONSTRAINT FK_BOOK_GENRE_BOOK_ID FOREIGN KEY (BOK_ID_C) REFERENCES T_BOOK (BOK_ID_C), CONSTRAINT FK_BOOK_GENRE_GENRE_ID FOREIGN KEY (GNR_ID_C) REFERENCES T_GENRE (GNR_ID_C) );
    ```
    - `T_LIBRARY_BOOK` stores the bookid of the books that are in the common library. The corresponding entity is `LibraryBook` and the dao is `LibraryBookDao`.
    ```sql
    create memory table T_LIBRARY_BOOK ( LBK_ID_C varchar(36) not null, LBK_IDBOOK_C varchar(36) not null, LBK_CREATEDATE_D datetime not null, LBK_DELETEDATE_D datetime, LBK_NUMRATINGS_I integer not null, LBK_AVGRATING_F float not null, primary key (LBK_ID_C) );
    ```
    - `T_LIBRARY_BOOK_RATING` stores the ratings for the books in the common library. A separate table is used instead of adding rating to the `T_USER_BOOK` table to allow for future extensibility and since we rate only books in the library, not in private bookshelves. Adding rating in `T_USER_BOOK` would not capture this notion as the rating of books not in the common library would forever remain 0. The corresponding entity is `LibraryBookRating` and the dao is `LibraryBookRatingDao`. 
    Schema:
    ```sql
    create memory table T_LIBRARY_BOOK_RATING ( LBR_ID_C varchar(36) not null, LBR_IDBOOK_C varchar(36) not null, LBR_IDUSER_C varchar(36) not null, LBR_RATING_I integer not null, LBR_CREATEDATE_D datetime not null, LBR_DELETEDATE_D datetime, primary key (LBR_ID_C) );
    ```

- For each table created which has an entity, an entry was added to `persistence.xml`.

- List of genres was added to Book, so a function was added in `UserDao` to set the genres for a book given its bookId. `GenreDao` was also made. It implements the `BaseDao` interface and has methods to create, get genre by id and get genre by name. It is also used to check if a genre exists and to get genres from the database to add to a book.

- `LibraryBookDao` was created which implements `BaseDao`. It has methods to create, get a library book by id, rate a library book, get a library book by book id, and get all library books

- `LibraryBookRatingDao` was created which implements `BaseDao`. It has methods to create and delete a library book rating object, get by id, and get by user id and book id

- `LibraryBookDto` was created which is used when listing the books in the common library

- bookId was added to `UserBookDto`

- `LibraryBookResource` was made to handle requests about the library books. Endpoints to add a book, list all books, get details about a book by book id, rate a book by bookid and get cover are implemented.

- Corresponding frontend changes were made as well. Two new controllers `Library` and `LibraryBook` were made along with their corresponding partials `library.html` and `library.book.html` to display the common library, add ratings and see genres. `bodok.view.html` was modified to show the genres.

Created an interface BaseDao with create and getById methods to be implemented by implementers since many Daos had this common functionality.


<@Prakhar documentation about strategy pattern stuff here>

## Detailed Documentation about changes made:
