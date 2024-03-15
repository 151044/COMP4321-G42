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
| typePrefix | Varchar (String) | True           |

## Word Table(s)
Note: Tables for stems in the body are named `body_{wordId}`.
For example, one such table might be `body_1`.

Tables for stems in titles are named `title_{wordId}`.
The corresponding example to the one above is `title_1`.

| Field Name | Data Type        | Is Primary Key |
|------------|------------------|----------------|
| docId      | Integer          | True           |
| paragraph  | Integer          | True           |
| sentence   | Integer          | True           |
| location   | Integer          | True           |
| suffix     | Varchar (String) | False          |
