- [ ] Creation date timestamp has type date in User class but type long in dto. Databases support Date though
- [ ] Authentication token dao uses authenticationTokenId in delete instead of object itself
- [ ] init and destroy of TokenBasedSecurityFilter are NOP

# Common Library
- [ ] Create table LibraryBook with bookid, genre, number of ratings, average rating... for books in the common library
(done without genre)

- [x] Create LibraryBookResource.java

- [ ] Create frontend page for library - partial, controller, ✅add to app.js
- [x] Instead of UserBookDto use LibraryBookDto in LibraryBookDao.getAllBooks

- [x] When adding a book to common library, just add it to the LibraryBook table (need LibraryBookDao for this)

- [x] When fetching all common library books, just get all entries in the LibraryBook table (need LibraryBookDao for this)

- [x] Cover on books in library

- [x] View book details by clicking on book from library (Use bookid instead of userbookid in viewing book details)

- [x] Create table LibraryBookRating with bookid, userid and rating

- [ ] check if dude has already rated this book

- [ ] **LibraryBookRatingDao get by criteria (by userid or bookid) to get the ratings for books rated by a user**

- [ ] When fetching ratings given by the user for common library books, get all entries in LibraryBookRating with same userid (need LibraryBookRatingDao for this)

- [x] When rating a book, (if not already rated) add an entry to the LibraryBookRating table with the userid, bookid and rating and send a notification to LibraryBook to update the average rating and number of ratings (observer pattern) (need LibraryBookRatingDao)
without observer pattern

- [ ] For filters/ranking use sql queries for order by rating desc or filter by author/genre (Can use strategy pattern here for different filters/rankings) (in LibraryBookDao) - prakhar

## For genres
- [x] Store genres in book class
- [x] initialise genres in the table
- [x] return genre for a book when returning the book
- [x] function in BookResource to edit the genres of a book: send a list of strings, which are the names of the genres
- [x] show genres in frontend
- [x] frontend checkboxes for selecting genre of a book
- [x] change flow to add genres when adding book to common library

## For public/private bookshelf
- [x] add visibility to tag class and table
- [x] update add and update tag functions to account for visibility
- [x] return public tags as well in list - order changes because set is not an ordered collection
- [ ] When updating tags of a book, check for ownership of the tag
<!-- - [ ] in TagDao.addTags and TagDao.removeTags verify ownership of tag -->
- [ ] in reading status verify ownership of tag 
- [ ] frontend