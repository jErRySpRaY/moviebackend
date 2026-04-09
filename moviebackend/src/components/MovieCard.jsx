import { Card, CardContent, Typography, Chip, Stack, Divider } from "@mui/material";

export default function MovieCard({ movie }) {
  if (!movie) return null;

  return (
    <Card
      sx={{
        maxWidth: 600,
        margin: "20px auto",
        padding: 2,
        borderRadius: 3,
        boxShadow: 4,
        background: "linear-gradient(135deg, #1e1e1e, #2c2c2c)",
        color: "white"
      }}
    >
      <CardContent>
        <Typography variant="h4" sx={{ fontWeight: "bold", mb: 1 }}>
          {movie.title}
        </Typography>

        <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
          <Chip label={movie.year} color="primary" />
          <Chip label={movie.runtime} color="secondary" />
          <Chip label={movie.rating} sx={{ backgroundColor: "#d32f2f", color: "white" }} />
        </Stack>

        <Typography variant="subtitle1" sx={{ opacity: 0.8, mb: 1 }}>
          {movie.genre} {movie.subgenre ? `• ${movie.subgenre}` : ""}
        </Typography>

        <Divider sx={{ my: 2, borderColor: "rgba(255,255,255,0.2)" }} />

        <Typography variant="body1" sx={{ lineHeight: 1.6 }}>
          {movie.plot}
        </Typography>
      </CardContent>
    </Card>
  );
}