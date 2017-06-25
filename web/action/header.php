<?php
ob_start();

function input_filter($input) {
  return strip_tags(trim($input));
}
?>