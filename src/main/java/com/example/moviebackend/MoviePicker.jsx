import { useState } from "react";

export default function MoviePicker() {
  const [genre, setGenre] = useState("Horror");
  const [result, setResult] = useState("");

  const pickMovie = async () => {
    const response = await fetch("http://localhost:8080/pickMovie");
    const text = await response.text();
    setResult(text);
  };

  return (
    <div style={{ padding: 20 }}>
      <h1>Movie Picker</h1>

      <label>Genre:</label>
      <select value={genre} onChange={(e) => setGenre(e.target.value)}>
        <option>Horror</option>
        <option>Comedy</option>
        <option>Action</option>
      </select>

      <button onClick={pickMovie} style={{ marginLeft: 10 }}>
        Find Movie
      </button>

      <h2 style={{ marginTop: 20 }}>{result}</h2>
    </div>
  );
}