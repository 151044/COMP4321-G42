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

## Word Index (WordIndex)
| Field Name | Data Type        | Is Primary Key |
|------------|------------------|----------------|
| stem       | Varchar (String) | False          |
| wordId     | Integer          | True           |

## Word Table(s)
Note: The name of each table is the word ID, followed by `_body`.
For example, one such table might be `1_body`.

Tables for stems in titles are named `{wordId}_title`.
The corresponding example to the one above is `1_title`.

| Field Name | Data Type        | Is Primary Key |
|------------|------------------|----------------|
| docId      | Integer          | True           |
| paragraph  | Integer          | True           |
| sentence   | Integer          | True           |
| location   | Integer          | True           |
| suffix     | Varchar (String) | False          |
