async function fetchJson(url, options) {
  const res = await fetch(url, options);
  const text = await res.text();

  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {}

  if (!res.ok) {
    throw new Error(
      data && data.error ? JSON.stringify(data) : (text || res.statusText)
    );
  }
  return data;
}

function fmtMinutes(m) {
  const h = Math.floor(m / 60);
  const r = m % 60;
  return h ? `${h}h ${r}m` : `${r}m`;
}

function renderResults(container, payload) {
  container.innerHTML = "";

  if (payload.count === 0) {
    container.innerHTML = `<p class="muted">No itineraries found.</p>`;
    return;
  }

  for (const it of payload.itineraries) {
    const card = document.createElement("div");
    card.className = "card";

    card.innerHTML = `
      <b>${it.currency} ${it.totalPrice}</b>
      · ${fmtMinutes(it.totalDurationMinutes)}
      · stops: ${it.stops}
    `;

    const table = document.createElement("table");
    table.innerHTML = `
      <thead>
        <tr>
          <th>Flight</th>
          <th>Route</th>
          <th>Departure</th>
          <th>Arrival</th>
          <th>Price</th>
        </tr>
      </thead>
      <tbody>
        ${it.legs.map(leg => `
          <tr>
            <td class="mono">${leg.flightId}</td>
            <td>${leg.origin} → ${leg.destination}</td>
            <td>${leg.departureLocal || leg.departureUtc}</td>
            <td>${leg.arrivalLocal || leg.arrivalUtc}</td>
            <td>${leg.currency} ${leg.price}</td>
          </tr>
        `).join("")}
      </tbody>
    `;

    card.appendChild(table);
    container.appendChild(card);
  }
}

function isValidAirport(code) {
  return /^[A-Z]{3}$/.test(code);
}

async function init() {
  const origin = document.getElementById("origin");
  const destination = document.getElementById("destination");
  const date = document.getElementById("date");
  const btn = document.getElementById("btn");
  const msg = document.getElementById("msg");
  const results = document.getElementById("results");

  // default values for demo
  origin.value = "JFK";
  destination.value = "LAX";
  date.value = "2026-02-10";

  btn.onclick = async () => {
    msg.textContent = "";
    results.innerHTML = "";

    const o = origin.value.toUpperCase().trim();
    const d = destination.value.toUpperCase().trim();

    if (!isValidAirport(o)) {
      msg.textContent = "Origin must be a 3-letter airport code (e.g. JFK)";
      return;
    }
    if (!isValidAirport(d)) {
      msg.textContent = "Destination must be a 3-letter airport code (e.g. LAX)";
      return;
    }
    if (o === d) {
      msg.textContent = "Origin and destination must be different.";
      return;
    }
    if (!date.value) {
      msg.textContent = "Please select a date.";
      return;
    }

    msg.textContent = "Searching…";

    try {
      const payload = await fetchJson("/api/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          origin: o,
          destination: d,
          date: date.value
        })
      });

      msg.textContent = "";
      renderResults(results, payload);

    } catch (e) {
      msg.textContent = "Search failed: " + e.message;
    }
  };
}

init();
