# Database Schema
## Document Table (Document)
| Field Name   | Data Type               | Is Primary Key |
|--------------|-------------------------|----------------|
| url          | Varchar (URL as String) | False          |
| docId        | Integer                 | True           |
| lastModified | Instant (java.time)     | False          |
| size         | Long (BIGINT)           | False          |
| title        | Varchar (String)        | False          |

The document table stores all the information related to a single indexed document. It can convert documents to document IDs (`docId`), which is used as a foreign key constraint for the other tables.

## Document Link Table (DocumentLink)
| Field Name | Data Type               | Is Primary Key |
|------------|-------------------------|----------------|
| docId      | Integer                 | True           |
| childUrl   | Varchar (URL as String) | True           |

The document link table stores all the children of an indexed document. `docId` is used as a foreign key.

## Word Index (WordIndex)
| Field Name | Data Type        | Is Primary Key |
|------------|------------------|----------------|
| stem       | Varchar (String) | False          |
| wordId     | Integer          | True           |
| typePrefix | Varchar (String) | True           |

The word index table stores all information related to a word, its stemmed form, and the word ID. `typePrefix` can either be `title` or `body`, and is used to distinguish between word IDs for text in the document tag and text in the body tag.

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
| rawWord*   | Varchar (String) | False          |

* The word will not be stored (an empty string will be stored instead) if the stemmed word is equal to the raw word.

The word table is used to store the relative positions of a word in a document. This acts as an inverted index. The index stores the stemmed word for searching and the raw word for future use in the search engine.
