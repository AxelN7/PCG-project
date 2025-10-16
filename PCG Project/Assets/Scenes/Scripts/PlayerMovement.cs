using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerMovement : MonoBehaviour
{
    [SerializeField] private float speed = 3f;
    private Rigidbody2D rb;
    private Vector2 movementDirection;

    [SerializeField] MapGenerator map;

    // Start is called before the first frame update
    void Start()
    {
        rb = GetComponent<Rigidbody2D>();
    }

    // Update is called once per frame
    void Update()
    {
        movementDirection = new Vector2(Input.GetAxis("Horizontal"), Input.GetAxis("Vertical"));    // Get movement input
    }

    private void FixedUpdate()
    {
        rb.velocity = movementDirection * speed;
    }
}
