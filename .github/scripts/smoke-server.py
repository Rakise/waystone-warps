"""Executed only on GitHub-hosted runners; verifies real Paper plugin startup."""
import hashlib
import json
import os
from pathlib import Path
import queue
import re
import shutil
import subprocess
import sys
import threading
import time
import urllib.request

if os.environ.get("GITHUB_ACTIONS") != "true":
    raise SystemExit("This server check is restricted to GitHub Actions.")

mode = sys.argv[1]
root = Path("build") / f"smoke-{mode}"
root.mkdir(parents=True, exist_ok=True)
plugins = root / "plugins"
plugins.mkdir(exist_ok=True)
agent = "WaystoneWarps-CI/1.2 (https://github.com/Rakise/waystone-warps)"


def fetch(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": agent}), timeout=120) as response:
        return response.read()


build = json.loads(fetch("https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/230"))
download = build["downloads"]["server:default"]
paper = fetch(download["url"])
assert hashlib.sha256(paper).hexdigest() == download["checksums"]["sha256"]
(root / "paper.jar").write_bytes(paper)
jar = next(path for path in Path("build/libs").glob("*.jar") if not path.name.endswith("-plain.jar"))
shutil.copy(jar, plugins / jar.name)
if mode == "floodgate":
    for project in ("floodgate", "geyser"):
        data = fetch(f"https://download.geysermc.org/v2/projects/{project}/versions/latest/builds/latest/downloads/spigot")
        (plugins / f"{project}.jar").write_bytes(data)
(root / "eula.txt").write_text("eula=true\n")
(root / "server.properties").write_text(
    "online-mode=false\nserver-port=25565\nview-distance=2\nsimulation-distance=2\n"
    "level-type=minecraft:flat\nspawn-protection=0\nmax-players=1\n")

process = subprocess.Popen(["java", "-Xms512M", "-Xmx1536M", "-jar", "paper.jar", "--nogui"],
                           cwd=root, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                           text=True, bufsize=1)
lines = queue.Queue()


def collect():
    for line in process.stdout:
        lines.put(line)


reader = threading.Thread(target=collect, daemon=True)
reader.start()
log = []
ready = False
deadline = time.monotonic() + 300
try:
    while time.monotonic() < deadline:
        try:
            line = lines.get(timeout=1)
        except queue.Empty:
            if process.poll() is not None:
                break
            continue
        log.append(line)
        print(line, end="", flush=True)
        if re.search(r'Done \([\d.,]+s\)!', line):
            ready = True
            process.stdin.write("plugins\nstop\n")
            process.stdin.flush()
            break
    if not ready:
        raise RuntimeError("Paper did not become ready within 300 seconds")
    process.wait(timeout=90)
    reader.join(timeout=5)
    while not lines.empty():
        line = lines.get_nowait()
        log.append(line)
        print(line, end="")
    output = "".join(log)
    assert "Enabling WaystoneWarps" in output, "Plugin enable was not attempted"
    assert "Disabling WaystoneWarps" in output, "Plugin was not active at shutdown"
    assert not re.search(r"Error occurred while enabling|Could not load.*Waystone|Failed to load.*Waystone|\[WaystoneWarps\].*(ERROR|Exception)", output)
    if mode == "floodgate":
        assert "Enabling floodgate" in output and "Enabling Geyser-Spigot" in output
    assert process.returncode == 0
finally:
    if process.poll() is None:
        process.kill()
        process.wait()
    (root / "smoke.log").write_text("".join(log))
