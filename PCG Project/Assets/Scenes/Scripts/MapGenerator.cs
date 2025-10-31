using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Unity.VisualScripting;
using UnityEditor;
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
    [SerializeField] private Vector2Int startPos;
    [SerializeField] private Vector2Int goalPos;

    public int MapWidth { get; private set; }
    public int MapHeight { get; private set; }

    private Vector2Int[] directions = new Vector2Int[]
    {
        new Vector2Int(1, 0),       // right
        new Vector2Int(-1, 0),      // left
        new Vector2Int(0, 1),       // up
        new Vector2Int(0, -1)       // down
    };

    // Start is called before the first frame update
    void Start()
    {
        LoadCSVFile();

        ValidateStartGoal();
        int numPaths = Bfs(startPos, goalPos, mapData, out int[,] dist);
        DrawShortestPaths(startPos, goalPos, mapData, dist);
        if (numPaths == 0)
        {
            Debug.LogWarning("Map invalid, regenerate");
        }
        else
        {
            Debug.Log("Map valid! Number of shortest paths: " + numPaths / 4);
        }
    }

    void LoadCSVFile()
    {
        if (csvFile == null)
        {
            Debug.LogError("No CSV file assigned!");
            return;
        }

        string[] rawLines = csvFile.text.Split(new[] { '\r', '\n' }, System.StringSplitOptions.RemoveEmptyEntries);
        var lines = rawLines.Where(l => !string.IsNullOrWhiteSpace(l)).ToArray();
        MapHeight = lines.Length;
        MapWidth = lines[0].Split(',').Length;

        mapData = new TileData[MapHeight, MapWidth];

        for (int y = 0; y < MapHeight; y++)                                // Each row from top to bottom
        {
            string line = lines[y].Trim();
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
                else
                {
                    mapData[y, x] = null;
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

    int Bfs(Vector2Int startPos, Vector2Int goalPos, TileData[,] mapData, out int[,] dist)
    {
        int rows = mapData.GetLength(0);
        int cols = mapData.GetLength(1);

        dist = new int[rows, cols];
        int[,] ways = new int[rows, cols];

        for (int y = 0; y < rows; y++)
        {
            for (int x = 0; x < cols; x++)
            {
                dist[y, x] = -1;            // Mark as unvisited
            }
        }

        Queue<Vector2Int> queue = new Queue<Vector2Int>();
        queue.Enqueue(startPos);

        dist[startPos.y, startPos.x] = 0;
        ways[startPos.y, startPos.x] = 1;

        while (queue.Count > 0)
        {
            var current = queue.Dequeue();

            foreach (var dir in directions)
            {
                Vector2Int next = current + dir;

                if (next.x < 0 || next.x >= cols || next.y < 0 || next.y >= rows) continue;

                if (!MoveFromTo(current.x, current.y, next.x, next.y, mapData)) continue;

                if (dist[next.y, next.x] == -1)             // Check if next is not visited and if so set the dist and ways
                {
                    dist[next.y, next.x] = dist[current.y, current.x] + 1;
                    ways[next.y, next.x] = ways[current.y, current.x];
                    queue.Enqueue(next);
                    //Debug.Log("Added tile to queue!");
                }
                else if (dist[next.y, next.x] == dist[current.y, current.x] + 1)        // Check if there is another shortest path to next
                {
                    ways[next.y, next.x] += ways[current.y, current.x];
                }
            }
        }
        if (dist[goalPos.y, goalPos.x] == -1) return 0;     // Goal not found
        Debug.Log("Shortest distance: " + dist[goalPos.y, goalPos.x]);
        return ways[goalPos.y, goalPos.x];                  // Number of shortest paths to the goal
    }

    bool MoveFromTo(int ax, int ay, int bx, int by, TileData[,] mapData)        // Check if can move from A to B
    {
        TileData a = mapData[ay, ax];       // Current-tile tiledata
        TileData b = mapData[by, bx];       // Next-tile tiledata

        if (a == null || b == null) return false;

        if (!a.canGoLeft && !b.canGoRight || !a.canGoRight && !b.canGoLeft) return false;   // Wall against wall checks
        if (!a.canGoUp && !b.canGoDown || !a.canGoDown && !b.canGoUp) return false;

        if (bx == ax + 1 && by == ay)       // Right
        {
            return a.canGoRight && b.canGoLeft;
        }

        if (bx == ax - 1 && by == ay)       // Left
        {
            return a.canGoLeft && b.canGoRight;
        }

        if (bx == ax && by == ay + 1)       // Up
        {
            return a.canGoUp && b.canGoDown;
        }

        if (bx == ax && by == ay - 1)       // Down
        {
            return a.canGoDown && b.canGoUp;
        }

        return false;
    }

    private void DrawShortestPaths(Vector2Int start, Vector2Int goal, TileData[,] mapData, int[,] dist)
    {
        Vector2Int current = goal;

        while (current != start)
        {
            Vector2Int prev = PrevStep(current, mapData, dist);

            if (prev == current) break;

            Vector3 worldCurrent = tileMap.GetCellCenterWorld(new Vector3Int(current.x, -current.y, 0));
            Vector3 worldPrev = tileMap.GetCellCenterWorld(new Vector3Int(prev.x, -prev.y, 0));
            Debug.DrawLine(worldCurrent, worldPrev, Color.cyan, 40f, false);
            current = prev;
        }
    }

    Vector2Int PrevStep(Vector2Int current, TileData[,] mapData, int[,] dist)
    {
        foreach (var dir in directions)
        {
            Vector2Int prev = current + dir;

            if (prev.x < 0 || prev.x >= dist.GetLength(1) || prev.y < 0 || prev.y >= dist.GetLength(0)) continue;

            if (dist[prev.y, prev.x] == dist[current.y, current.x] - 1 && MoveFromTo(prev.x, prev.y, current.x, current.y, mapData)) return prev;
        }
        return current;
    }

    bool ValidateStartGoal()
    {
        if (startPos.x < 0 || startPos.x >= MapWidth || startPos.y < 0 || startPos.y >= MapHeight)
        {
            Debug.LogError($"Start out of bounds:{startPos} MapSize={MapWidth}x{MapHeight}");
            return false;
        }

        if (goalPos.x < 0 || goalPos.x >= MapWidth || goalPos.y < 0 || goalPos.y >= MapHeight)
        {
            Debug.LogError($"Goal out of bounds:{goalPos} MapSize={MapWidth}x{MapHeight}");
            return false;
        }

        Debug.Log($"Start tile is {(mapData[startPos.y, startPos.x] == null ? "Null" : "Valid")}");
        Debug.Log($"Goal tile is {(mapData[goalPos.y, goalPos.x] == null ? "Null" : "Valid")}");
        return true;
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
