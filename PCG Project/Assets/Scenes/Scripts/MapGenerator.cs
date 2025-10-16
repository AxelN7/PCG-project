using System.Collections;
using System.Collections.Generic;
using System.IO;
using UnityEngine;
using UnityEngine.Tilemaps;

public class MapGenerator : MonoBehaviour
{
    private int[,] map =
    {
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 1, 1, 1, 0, 0},
        {0, 0, 0, 1, 0, 1, 0, 1, 0, 0},
        {0, 0, 0, 1, 1, 1, 0, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
        {0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    [SerializeField] private GameObject wall;
    [SerializeField] private GameObject floor;

    private Tilemap tileMap;
    [SerializeField] private TileBase[] tiles;
    private TextAsset csvFile;
    //private int[,] mapData;

    public int MapWidth { get { return map.GetLength(1); } }
    public int MapHeight { get { return map.GetLength(0); } }

    // Start is called before the first frame update
    void Start()
    {
        GenerateMap();
        //LoadCSVFile();
    }

    void GenerateMap()
    {
        Vector2 startPos = new Vector2(-MapWidth / 2f + 0.5f, MapHeight / 2f - 0.5f);

        for (int y = 0; y < MapHeight; y++)
        {
            for (int x = 0; x < MapWidth; x++)
            {
                GameObject tile = map[y, x] == 1 ? floor : wall;
                float tileSize = 1f;
                Vector2 pos = new Vector2(startPos.x + x * tileSize, startPos.y - y * tileSize);    // Generate from the center of the camera
                Instantiate(tile, pos, Quaternion.identity);
            }
        }
    }

    void LoadCSVFile()
    {
        if (csvFile == null)
        {
            Debug.LogError("No CSV file assigned!");
            return;
        }

        string path = Path.Combine(Application.streamingAssetsPath, "map.csv");
        string[] lines = File.ReadAllLines(path);
        int height = lines.Length;
        int width = lines[0].Split(',').Length;
        
        for (int y = 0; y < height; y++)                                // Each row from top to bottom
        {
            string line = lines[y].Trim();
            if (string.IsNullOrEmpty(line)) continue;

            string[] values = line.Split(',');

            for (int x = 0; x < width; x++)                             // Each column from left to right
            {
                if (int.TryParse(values[x], out int tileIndex))         // Convert string to int
                {
                    if (tileIndex >= 0 && tileIndex < tiles.Length)
                    {
                        Vector3Int pos = new Vector3Int(x, -y, 0);      // Tile position on the tilemap

                        Tile t = ScriptableObject.Instantiate(tiles[tileIndex]) as Tile;            // Clone tile to change collider type
                        if (tileIndex == 4 || tileIndex == 10 || tileIndex == 13)
                        {
                            t.colliderType = Tile.ColliderType.None;
                        }
                        else
                        {
                            t.colliderType = Tile.ColliderType.Grid;
                        }
                        tileMap.SetTile(pos, t);
                    }
                }
            }
        }
    }

    private void OnDrawGizmos()
    {
        if (map == null) return;

        float tileSize = 1f;
        Vector2 startPos = new Vector2(-MapWidth / 2f + 0.5f, MapHeight / 2f - 0.5f);

        for (int y = 0; y < MapHeight; y++)
        {
            for (int x = 0; x < MapWidth; x++)
            {
                Vector2 pos = new Vector2(startPos.x + x * tileSize, startPos.y - y * tileSize);
                Gizmos.color = map[y, x] == 1 ? Color.green : Color.red;
                Gizmos.DrawWireCube(pos, Vector3.one * 0.9f);
            }
        }
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
