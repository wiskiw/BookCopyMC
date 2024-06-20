# Book Copy (Extended)

This is a fork of the client-side [eclipseisoffline's](https://github.com/eclipseisoffline) [Book Copy](https://github.com/eclipseisoffline/bookcopy/tree/main) mod.

> This mod adds a simple client-side `/bookcopy` command, allowing you to save written books and import them later into another book.

## Main Feature
* **NBT Export/Import**: Support for exporting and importing books to NBT files.
* **JSON Export/Import**: Support for exporting and importing books to JSON files using a proprietary format.
* **Autosign Option**: Added an autosign option for the import command.
* **Improved Format Support**: Simplified the process of adding new import/export formats.

## Features

### Export Book
You can export signed and unsigned books (Book and Quill) that the player holds in the main hand using the following command:
```
/bookcopy export <filename> <format>
```
- `<filename>` - Destination filename stored in the `.minecraft/config/bookcopy/` folder.
- `<format>` - One of the supported export formats: `-json`, `-nbt`.

Usage examples:
```
/bookcopy export firstbook.nbt -nbt
/bookcopy export secondbook.json -json
/bookcopy export otherbook.txt -json
```

### Import Book
You can import and sign book content to the Book and Quill that the player holds in the main hand using the following command:
```
/bookcopy import <filename> <format> [sign]
```
- `<filename>` - Source filename stored in the `.minecraft/config/bookcopy/` folder.
- `<format>` - One of the supported export formats: `-json`, `-nbt`.
- `[sign]` - Optional flag that will sign the book if the book's title exists in the imported file.

Usage examples:
```
/bookcopy import firstbook.nbt -nbt
/bookcopy import secondbook.json -json sign
/bookcopy import otherbook.txt -json sign
```

## Format Examples
### JSON
```json
{
  "title": "Book Name Here",
  "author": "Player22",
  "pages": [
    [
      "The first line",
      "",
      "Third line, page 1",
      "An empty line is next",
      ""
    ],
    [
      "Line 1, page 2"
    ]
  ]
}
```

### NBT
```nbt
"": {
	title: Book Name Here
	author: Player845
	pages: [ "The first line\n\nThird line, page 1\nAn empty line is next\n", "Line 1, page 2", ]
}
```