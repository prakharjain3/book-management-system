SYSTEM = """
     You are a Software Engineer. 
     Your role is to find the design and code smells and refactor them given the code related to one of the classes in the codebase.
     This is the plantuml code of the codebase to be refactored.
     @startuml "Book Addition and Display Subsystem"

enum BaseFunction {
  ADMIN
}

abstract class BaseResource {
  # HttpServletRequest request;
  # String appKey;
  # IPrincipal principal;

  # boolean authenticate()
  # void checkBaseFunction(BaseFunction baseFunction)
  # boolean hasBaseFunction(BaseFunction baseFunction)
}

class BookResource extends BaseResource {
  + add(isbn: String): Response
  + delete(userBookId: String): Response
  + add(title: String, subtitle: String, author: String, description: String, isbn10: String, isbn13: String, pageCount: Long, language: String, publishDateStr: String, tagList: List<String>): Response
  + update(userBookId: String, title: String, subtitle: String, author : String,description : String, isbn10: String, isbn13: String, pageCount : Long, language: String, publishDateStr : String, tagList : List<String>) : Response
  + get(userBookId: String) : Response
  + cover(userBookId: String) : Response
  + updateCover(userBookId: String, imageUrl : String) : Response
  + list(limit : Integer, offset : Integer, sortColumn : Integer, asc : Boolean, search : String, read : Boolean, tagName : String) : Response
  + importFile(fileBodyPart:FormDataBodyType): Response
  + read(userBookId: String, read : boolean): Response
}


class Book {
  - id: Long
  - title: String
  - author: String
  - publishedDate: Date
}

class User {
  - id: String
  - localeId: String
  - roleId: String
  - username: String
  - passwor
  - email: String
  - theme: String
  - firstConnection: Boolean
  - createDate: Date
  - deleteDate: Date
}

class BookDao {
  + create(book: Book): Book.id
  + getById(id: String): Book
  + getByIsbn(isbn: String): Book
}

class UserDao {
  + authenticate(username: String, password: String): User
  + create(user: User): String
  + update(user: User): User
  + updatePassword(user: User): User
  + getById(id: String): User
  + getByUsername(username: String): User
  + getActiveByPasswordResetKey(passwordResetKey: String): User
  + delete(username: String): void
  # hashPassword(password: String): String
  + findAll(paginatedList: PaginatedList<UserDto>, sortCriteria: SortCriteria): void
}

class UserBookDao {
  + create(userBook: UserBook): String
  + delete(id: String): void
  + getUserBook(userBookId: String, userId: String): UserBook
  + getUserBook(userBookId: String): UserBook
  + getByBook(bookId: String, userId: String): UserBook
  + findByCriteria(paginatedList: PaginatedList<UserBookDto>, criteria: UserBookCriteria, sortCriteria: SortCriteria): void
}

class UserBookDto {
  - id: String
  - title: String
  - subtitle: String
  - author: String
  - lnaguage: String
  - publishTimestamp: Long
  - createTimestamp: Long
  - readTimestamp: Long
}

class UserBook {
  - {static} serialVersionUID: Long = 1L {readOnly}
  - id: String
  - bookId: String
  - userId: String
  - createDate: Date
  - deleteDate: Date
  - readDate: Date

  + hashCode(): Integer
  + equals(obj: Object): Boolean
  + toString(): String
}

class UserBookCriteria {
    - userId: String
    - search: String
    - read: Boolean
    - tagIdList: List<String>
    }

class SortCriteria {
  - column: Integer
  - asc: Boolean
  + getColumn(): Integer
  + isAsc(): Boolean
}

class PaginatedList {
  - limit: Integer
  - offset: Integer
  - resultCount: Integer
  - resultList: List<T>
}

class PaginatedLists {
  - {static} DEFAULT_PAGE_SIZE: Integer = 10
  - {static} MAX_PAGE_SIZE: Integer: 100

  + {static} create(pageSize: Integer, offset: Integer): PaginatedList<E>
  + {static} create(): PaginatedList<E>
  + {static} executeCountQuery(paginatedList: PaginatedList<E>, queryParam: QueryParam): void
  - executeResultQuery(paginatedList: PaginatedList<E>, queryParam: QueryParam): List<E>
  + executePaginatedQuery(paginatedList: PaginatedList<E>, queryParam: QueryParam): List<E>
  + executePaginatedQuery(paginatedList: PaginatedList<E>, queryParam: QueryParam, sortCriteria: SortCriteria): List<E>
}

class Tag {
  - id: String
  - name: String
  - useriD: String
  - createDate: Date
  - deleteDate: Date
  -color: String

}

class TagDao {
- getById(id: String): Tag
- getByUserId(userId: String): List<Tag>          
- updateTagList(userBookId: String, tagIdSet: Set<String>): void      
- getByUserBookId(userBookId: String): List<TagDto> 
- create(tag: Tag): String            
- getByName(userId: String, name: String): Tag               
- getByTagId(userId: String, tagId: String): Tag           
- delete(tagId: String): void               
- findByName(userId:String, name: String): List<Tag>

}

class ValidationUtil {
  - {static} EMAIL_PATTERN: Pattern
  - {static} HTTP_URL_PATTERN: Pattern
  - {static} ALPHANUMERIC_PATTERN: Pattern
  + validateRequired(s: Object, name: String): void
  + validateLength(s: String, name: String, lengthMin: Integer, lengthMax: Integer, nullable: Boolean): String
  + validateLength(s: String, name: String, lengthMin: Integer, lengthMax: Integer): String
  + validateStringNotBlank(s: String, name: String): String
  + validateHexColor(s: String, name: String, nullable: Boolean): void
  + validateEmail(s: String, name: String): void
  + validateHttpUrl(s: String, name: String): String
  + validateAlphanumeric(s: String, name: String): void
  + validateDate(s: String, name: String, nullable: Boolean): Date
  + validateLocale(localeId: String, name: String, nullable: Boolean): String
  + validateTheme(themeId: String, name: String, nullable: Boolean): String
}


class DirectoryUtil {
  + {static} getBaseDataDirectorry() : File
  + {static} getDbDirectory() : File
  + {static} getBookDirectory() : File
  + {static} getLogDirectory() : File
  + {static} getThemeDirectory() : File
  - {static} getDataSubDirectory() : File
}

class AppContext {
  - {static} instance : AppContext
  - eventBus : EventBus
  - asyncEventBus : EventBus
  - importEventBus : EventBus
  - bookDataService : BookDataService
  - facebookService : FacebookService
  - asyncExecutorList : List<ExecutorService>
  - resetEventBus() : void
  + {static} getInstance() : AppContext
  - newAsyncEventBus() : EventBus
}


class BookImportedEvent {
  - user: User
  - importFile: File
}

class TagDto {
  - id: String
  - name: String
  - color: String
}

User "1..*" -- "0..*" Book
(User, Book) .. BookResource: creates, adds, deletes


BookResource --> ValidationUtil: validates\n<<uses>>
BookResource "1" *-- "1" BookDao

Book "0..*" --o "1" BookDao

BookResource --> AppContext: get book from a public API\n<<uses>>
BookResource "1" *-- "1" UserBookDao
UserBookDao "1" o-- "0..*" UserBook

UserBookDao "1" -- "0..*" UserBookDto: <<uses>> 
UserBookDao --> UserBookCriteria: <<uses>>
UserBookDao --> PaginatedLists: <<uses>>
UserBookDao --> SortCriteria: <<uses>>

BookResource --> UserDao: <<uses>>

BookResource "1" *-- "1" TagDao
TagDao "1" o-- "0..*" Tag

BookResource --> DirectoryUtil: get cover image\n<<uses>>

BookResource --> PaginatedLists: creates list of books\n<<uses>>
PaginatedLists "1" -- "1..*" PaginatedList: Utilities for paginates list >

BookResource --> SortCriteria: <<uses>>
BookResource --> UserBookCriteria: <<uses>>

BookResource --> UserBookDto: <<uses>>
BookResource --> BookImportedEvent: import file\n<<uses>>

BookImportedEvent "0..*" -- "1..*" User

UserDao --> PaginatedLists: <<uses>>
UserDao --> SortCriteria: <<uses>>

(BookResource, TagDao) .. TagDto


@enduml
"""

SMELL = "This is the class that you have to refactor. Give the design and code smells that you find. Do not give any refactored code and only the smells that you find in this codebase. \n\n"

REFACTOR = "Produce the refactored code for the given class according to the design smells generated in the previous step. Only give the code and no other information. Give valid Java code that can be executed directly. Also add comments which list out the function signatures of all functions and variables. \n\n"