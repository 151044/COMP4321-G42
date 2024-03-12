# Database Schema
## Document Table (Document)
| Field Name   | Data Type           | Is Primary Key |
|--------------|---------------------|----------------|
| url          | Varchar (String)    | False          |
| docId        | Integer             | True           |
| lastModified | Instant (java.time) | False          |
| size         | Long (BIGINT)       | False          |

## Document Link Table (DocumentLink)
| Field Name | Data Type | Is Primary Key |
|------------|-----------|----------------|
| docId      | Integer   | True           |
| childId    | Integer   | True           |

## Word Table(s)
Note: The name of each (stemmed) word is the table name, followed by `_body`.
For example, one such table might be `Comput_body`.

Tables for stems in titles are named `{stem}_title`.
The corresponding example to the one above is `Comput_title`.

| Field Name | Data Type | Is Primary Key |
|------------|-----------|----------------|
| docId      | Integer   | True           |
| paragraph  | Integer   | True           |
| sentence   | Integer   | True           |
| location   | Integer   | True           |
