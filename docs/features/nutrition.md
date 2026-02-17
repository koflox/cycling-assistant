# Nutrition

The nutrition feature provides tracking of nutrition data linked to cycling sessions.

## Overview

Riders can log nutrition intake associated with their cycling sessions. The feature integrates with the session module through the `nutrition-session` bridge and with settings through the `nutrition-settings` bridge.

## Bridge Connections

| Bridge                | Purpose                                  |
|-----------------------|------------------------------------------|
| `nutrition-session`   | Links nutrition entries to session data  |
| `nutrition-settings`  | Exposes nutrition preferences to settings |
| `destination-nutrition`| Connects destination info with nutrition |
