using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Tilemaps;

public class TileData : MonoBehaviour
{
    public Sprite sprite;
    public Tile.ColliderType colliderType = Tile.ColliderType.None;
    public bool canGoUp = false;
    public bool canGoDown = false;
    public bool canGoLeft = false;
    public bool canGoRight = false;
}
