(function() {
 var cube, race, square,
  __slice = Array.prototype.slice;

 square = function(x) {
  return x * x;
 };

 cube = function(x) {
  return square(x) * x * x;
 };

 race = function() {
  var runners, winner;
  winner = arguments[0], runners = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
  return print(winner, runners);
 };

}).call(this);
