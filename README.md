# DND-Encounter-Tracker
## Project Description

This project will be a web-based Dungeons & Dragons initiative tracker designed to help Dungeon Masters organize combat encounters. Users will be able to create campaigns, manage party members, create encounters, and track initiative order during combat. The application will automatically include active party members in newly created encounters and sort combatants by initiative as values are entered. During combat, users will be able to advance or reverse the turn order and mark combatants as defeated so they are skipped during initiative while remaining visible.

## Objectives
Develop a secure web application with user authentication.
Allow users to create and manage campaigns and encounters.
Automatically add active party members to new encounters.
Display and automatically sort combatants by initiative.
Provide controls for advancing through combat turns.
Deploy the application to a cloud environment using Docker.

## Cloud Platform

The application will be deployed to either Amazon Web Services (AWS) or Google Cloud Platform (GCP). The backend application and database will be hosted in the cloud, allowing users to access the application through a web browser.

## Deployment Approach

The application will be developed using Spring Boot with a PostgreSQL database. The application will be containerized using Docker and deployed to a cloud virtual machine. Source code will be managed with GitHub, and if time permits, a simple CI/CD pipeline will be implemented to automate deployment.
