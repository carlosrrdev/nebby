<div align="center">
<img src="nebby-large.png" alt="nebby logo" />
<h1>Nebby</h1>
<p>A directory indexer GUI application</p>
</div>

> This is a work in progress. Please report any bugs or inconsistencies

## Goal
 I created Nebby because I felt like the native way to search for a directory on a computer is often very slow.
 It obviously depends on the amount of directories you may have but generally the process is slow with the biggest
 offender being the Windows operating system.
 
The goal of Nebby is to provide a faster and alternative way to search through large amount of directories by "caching"
the paths to directories in an SQLite file.

## Process

The process is straight-forward.
- Select the parent directory
- Crawl through all child directories
- Grab filename and full path to each child directory
- Optionally split the filenames and create custom SQL table columns
- Create and save a new SQL file containing the filenames and paths (and any other custom columns) of each child directory.

Most recently opened SQL file is saved and automatically opened when the application launches.

---

<div align="center">
<h3>With custom splitter and table columns (recommended)</h3>
<img src="clip1.gif" alt="custom columns" />
</div>

<div align="center">
<h3>Using default table columns</h3>
<img src="clip2.gif" alt="default" />
</div>

---

## Invalid files

There is an option to skip files that do not meet the criteria whenever a splitter and custom columns is provided. 
Give the following scenario:

Filenames will be split by underscore (_) and custom table columns set to `cus1,cus2,cus3`.

- `my_custom_dir` ***does*** meet the criteria and will be added to the table
- `my_custom_dir_path` ***does not*** meet the criteria and will be either ignored or added into a separate table.

It is recommended that the default option `Create separate table` is used since this gives you the opportunity to review
directories that were excluded from the main table.

## Tech

- Java (21.0.6)
- Swing
- FlatLaf (with IntelliJ theme addon)
- JDBC (SQLite)