"""
Basic usage example for Browser4 Python SDK.

This example demonstrates fundamental Browser4 SDK operations:
- Creating a client and session
- Loading pages
- Navigating with WebDriver
- Extracting data with CSS selectors
- AI-powered actions

Prerequisites:
- A running Browser4 server at http://localhost:8182
- Or set up your own server URL
"""

from pulsar_sdk import PulsarClient, AgenticSession

def main():
    print("=== Browser4 Python SDK - Basic Usage Example ===\n")

    # Create client and session
    print("Creating session...")
    client = PulsarClient(base_url="http://localhost:8182")
    
    try:
        session_id = client.create_session()
        print(f"Session created: {session_id}\n")
        
        session = AgenticSession(client)
        
        # Load a page
        print("Loading example.com...")
        page = session.open("https://example.com")
        print(f"Page loaded: {page.url}")
        print(f"Content type: {page.content_type}")
        print(f"Content length: {page.content_length} bytes\n")
        
        # Parse the page
        document = session.parse(page)
        
        # Extract data using CSS selectors
        if document:
            print("Extracting data with CSS selectors...")
            fields = session.extract(document, {
                "title": "h1",
                "description": "p"
            })
            
            print("Extracted data:")
            for key, value in fields.items():
                print(f"  {key}: {value[:100] if value else 'None'}...")
            print()
        
        # Use WebDriver for navigation
        driver = session.driver
        print("Getting current URL from WebDriver...")
        current_url = driver.current_url()
        print(f"Current URL: {current_url}")
        print(f"Page title: {driver.title()}\n")
        
        # AI-powered action (requires AI capabilities)
        try:
            print("Attempting AI-powered action...")
            result = session.act("scroll to the bottom of the page")
            print(f"Action success: {result.success}")
            print(f"Action message: {result.message}\n")
        except Exception as e:
            print(f"AI action skipped: {e}\n")
        
        # Clean up
        print("Closing session...")
        session.close()
        print("Session closed successfully!")
        
    except Exception as e:
        print(f"Error: {e}")
        print("\nNote: This example requires a running Browser4 server.")
        print("Start the server with: java -jar Browser4.jar")
    finally:
        client.close()
    
    print("\n=== Example completed ===")


if __name__ == "__main__":
    main()
