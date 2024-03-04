# Database Schema
## Document Table
| Field Name   | Data Type                | Is Primary Key |
|--------------|--------------------------|----------------|
| URL          | Varchar (String)         | True           |
| DocId        | Integer                  | False          |
| LastModified | Integer (Unix Timestamp) | False          |

## Word Table
Note: The name of each (stemmed) word is the table name.

| Field Name | Data Type | Is Primary Key |
|------------|-----------|----------------|
| DocId      | Integer   | True           |
| Paragraph  | Integer   | True           |
| Sentence   | Integer   | True           |
| Location   | Integer   | True           |