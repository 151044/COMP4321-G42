# Database Schema
## Document Table
| Field Name   | Data Type                | Is Primary Key |
|--------------|--------------------------|----------------|
| URL          | Varchar (String)         | True           |
| DocId        | Integer                  | False          |
| LastModified | Integer (Unix Timestamp) | False          |
| Size         | Integer                  | False          |

## Document Link Table
| Field Name | Data Type | Is Primary Key |
|------------|-----------|----------------|
| DocId      | Integer   | True           |
| ChildId    | Integer   | False          |

## Word Table(s)
Note: The name of each (stemmed) word is the table name.

Tables for stems in titles are named `{stem}_title`.

| Field Name | Data Type | Is Primary Key |
|------------|-----------|----------------|
| DocId      | Integer   | True           |
| Paragraph  | Integer   | True           |
| Sentence   | Integer   | True           |
| Location   | Integer   | True           |