square = (x) -> x * x
cube   = (x) -> square(x) * x * x

# Splats:
race = (winner, runners...) ->
  print winner, runners