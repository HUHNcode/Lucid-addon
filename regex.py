import re
# beispiel 1
game_message = "<player345> the message"
message_format = "<PLAYER>\s"

# beispiel 2
game_message = "OBSIDIAN ✦ 4NESCA › how much for em to neth upgrade (2)"
message_format = ".*\sPLAYER\s›\s"

message_format_re = message_format.replace("PLAYER", r"(\w+)")

player = re.search(message_format_re, game_message)

if player:
    message = game_message[player.end():]  # Schneidet alles ab dem Ende des Matches aus
    print(player.group(1))
    print(message.strip())  # Entfernt eventuell führende Leerzeichen
else:
    print("Kein Spieler gefunden / regex fehler")