{ pkgs ? import <nixpkgs> {} }:
let
  runtimeLibs = with pkgs; lib.makeLibraryPath [
    glfw
    libGL
    libX11
    libXcursor
    libXext
    libXrandr
    libXxf86vm
    libglvnd
  ];
in pkgs.mkShell {
  shellHook = ''
    export LD_LIBRARY_PATH=${runtimeLibs}:$LD_LIBRARY_PATH
  '';
}
