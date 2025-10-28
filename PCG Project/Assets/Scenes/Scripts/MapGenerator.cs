using System.Collections;
using System.Collections.Generic;
using System.IO;
using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.Tilemaps;
using static Unity.VisualScripting.Member;
using static UnityEditor.IMGUI.Controls.PrimitiveBoundsHandle;
using static UnityEngine.UIElements.UxmlAttributeDescription;

public class MapGenerator : MonoBehaviour
{
    [SerializeField] private Tilemap tileMap;
    [SerializeField] private TileData[] tileArray;
    [SerializeField] private TextAsset csvFile;
    private TileData[,] mapData;
    [SerializeField] private Vector2Int playerPos;
    [SerializeField] private Vector2Int goalPos;

    public int MapWidth { get; private set; }
    public int MapHeight { get; private set; }

    // Start is called before the first frame update
    void Start()
    {
        LoadCSVFile();

        bool mapValid = Bfs(playerPos, goalPos, mapData);       // Run BFS to check that the map is valid
        if (!mapValid)
        {
            Debug.LogWarning("Map invalid, regenerate");
        }
        else
        {
            Debug.Log("Map valid!");
        }
    }

    void LoadCSVFile()
    {
        if (csvFile == null)
        {
            Debug.LogError("No CSV file assigned!");
            return;
        }

        string[] lines = csvFile.text.Split('\n');
        MapHeight = lines.Length;
        MapWidth = lines[0].Split(',').Length;

        mapData = new TileData[MapHeight, MapWidth];

        for (int y = 0; y < MapHeight; y++)                                // Each row from top to bottom
        {
            string line = lines[y].Trim();
            if (string.IsNullOrEmpty(line)) continue;

            string[] cells = line.Split(',');

            for (int x = 0; x < cells.Length; x++)                             // Each column from left to right
            {
                int tileIndex = int.Parse(cells[x]);
                if (tileIndex >= 0 && tileIndex < tileArray.Length)
                {
                    TileData tileData = tileArray[tileIndex];
                    mapData[y, x] = tileData;

                    Tile tile = ScriptableObject.CreateInstance<Tile>();
                    tile.sprite = tileData.sprite;
                    tile.colliderType = tileData.colliderType;

                    Vector3Int pos = new Vector3Int(x, -y, 0);
                    tileMap.SetTile(pos, tile);
                }
            }
        }

        CenterMap();
    }

    void CenterMap()
    {
        BoundsInt bounds = tileMap.cellBounds;
        Vector3 mapCenter = tileMap.localBounds.center;

        Camera cam = Camera.main;
        cam.transform.position = new Vector3(mapCenter.x - 7, mapCenter.y + 5, cam.transform.position.z);
    }

    bool Bfs(Vector2Int startPos, Vector2Int goalPos, TileData[,] mapData)
    {
        Vector2Int[] directions = new Vector2Int[]
        {
            new Vector2Int(1, 0),       // right
            new Vector2Int(-1, 0),      // left
            new Vector2Int(0, 1),       // up
            new Vector2Int(0, -1)       // down
        };

        int rows = mapData.GetLength(0);
        int cols = mapData.GetLength(1);

        Queue<Vector2Int> queue = new Queue<Vector2Int>();
        HashSet<Vector2Int> visited = new HashSet<Vector2Int>();

        queue.Enqueue(startPos);
        visited.Add(startPos);

        while (queue.Count > 0)
        {
            var current = queue.Dequeue();

            if (current == goalPos) return true;

            foreach (var dir in directions)
            {
                Vector2Int next = current + dir;

                if (next.x < 0 || next.x >= cols || next.y < 0 || next.y >= rows) continue;

                if (visited.Contains(next)) continue;

                if (MoveFromTo(current.x, current.y, next.x, next.y, mapData))    // Check for a valid path through the next tile
                {
                    queue.Enqueue(next);
                    visited.Add(next);
                    Debug.Log("Added tile to queue!");
                }
            }
        }
        return false;
    }

    bool MoveFromTo(int ax, int ay, int bx, int by, TileData[,] mapData)        // Check if can move from A to B
    {
        TileData a = mapData[ay, ax];       // Current-tile tiledata
        TileData b = mapData[by, bx];       // Next-tile tiledata

        if (a == null || b == null) return false;

        if (bx == ax + 1 && by == ay)       // right
        {
            return a.canGoRight && b.canGoLeft;
        }

        if (bx == ax - 1 && by == ay)       // left
        {
            return a.canGoLeft && b.canGoRight;
        }

        if (bx == ax && by == ay + 1)       // up
        {
            return a.canGoUp && b.canGoDown;
        }

        if (bx == ax && by == ay - 1)       // down
        {
            return a.canGoDown && b.canGoUp;
        }

        return false;
    }

    //private void OnDrawGizmos()
    //{
    //    if (map == null) return;

    //    float tileSize = 1f;
    //    Vector2 startPos = new Vector2(-MapWidth / 2f + 0.5f, MapHeight / 2f - 0.5f);

    //    for (int y = 0; y < MapHeight; y++)
    //    {
    //        for (int x = 0; x < MapWidth; x++)
    //        {
    //            Vector2 pos = new Vector2(startPos.x + x * tileSize, startPos.y - y * tileSize);
    //            Gizmos.color = map[y, x] == 1 ? Color.green : Color.red;
    //            Gizmos.DrawWireCube(pos, Vector3.one * 0.9f);
    //        }
    //    }
    //}

    // Update is called once per frame
    void Update()
    {
        
    }
}
