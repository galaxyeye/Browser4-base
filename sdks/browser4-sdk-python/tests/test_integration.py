"""
Integration tests for Browser4 Python SDK.

These tests use real Browser4 and Mock servers, similar to the
Kotlin SDK integration tests. They verify end-to-end functionality
with actual server instances.

To run these tests:
    pytest tests/test_integration.py -v -s

Note: Tests require Maven build and will start servers automatically.
The first run may take longer due to build and server startup.
"""
import pytest
from pathlib import Path
import sys

# Ensure the SDK is importable
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from browser4 import (
    PulsarClient,
    PulsarSession,
    AgenticSession,
    WebDriver,
    WebPage,
    NormURL,
)

from test_urls import (
    SIMPLE_PAGE,
    PRODUCT_LIST,
    SIMPLE_DOM,
    MOCK_SERVER_BASE,
)


@pytest.mark.integration
class TestPulsarClientIntegration:
    """Integration tests for PulsarClient with real server."""

    def test_create_and_delete_session(self, integration_client):
        """Test that we can create and delete sessions with real server."""
        client = integration_client
        
        # Session should already be created by fixture
        assert client.session_id is not None
        assert len(client.session_id) > 0
        
        # Should be able to delete
        client.delete_session()
        
        # Create a new one
        new_session_id = client.create_session()
        assert new_session_id != client.session_id  # Different from previous
        client.session_id = new_session_id

    def test_create_session_with_capabilities(self, integration_client):
        """Test creating session with custom capabilities."""
        client = integration_client
        
        # Delete existing session
        client.delete_session()
        
        # Create with capabilities
        capabilities = {
            "browserName": "chrome",
            "pageLoadStrategy": "normal"
        }
        session_id = client.create_session(capabilities=capabilities)
        
        assert session_id is not None
        assert len(session_id) > 0
        client.session_id = session_id

    def test_navigate_to_url(self, integration_client):
        """Test basic URL navigation."""
        client = integration_client
        
        # Navigate to simple page
        response = client.post(
            f"/session/{client.session_id}/url",
            {"url": SIMPLE_PAGE}
        )
        
        assert response is not None

    def test_get_current_url(self, integration_client):
        """Test retrieving current URL."""
        client = integration_client
        
        # Navigate first
        client.post(
            f"/session/{client.session_id}/url",
            {"url": SIMPLE_PAGE}
        )
        
        # Get current URL
        result = client.get(f"/session/{client.session_id}/url")
        assert result is not None


@pytest.mark.integration
class TestPulsarSessionIntegration:
    """Integration tests for PulsarSession with real server."""

    def test_normalize_url(self, integration_client):
        """Test URL normalization with real server."""
        client = integration_client
        session = PulsarSession(client)
        
        result = session.normalize(SIMPLE_PAGE, args="-expires 1d")
        
        assert isinstance(result, NormURL)
        assert result.url == SIMPLE_PAGE
        assert not result.is_nil

    def test_open_page(self, integration_client):
        """Test opening a page immediately."""
        client = integration_client
        session = PulsarSession(client)
        
        page = session.open(SIMPLE_PAGE)
        
        assert isinstance(page, WebPage)
        assert not page.is_nil
        assert page.url is not None

    def test_load_page(self, integration_client):
        """Test loading a page (with caching)."""
        client = integration_client
        session = PulsarSession(client)
        
        page = session.load(SIMPLE_PAGE, args="-expires 1d -parse")
        
        assert isinstance(page, WebPage)
        assert not page.is_nil
        assert page.url is not None

    def test_submit_url(self, integration_client):
        """Test submitting URL to crawl pool."""
        client = integration_client
        session = PulsarSession(client)
        
        result = session.submit(SIMPLE_PAGE)
        
        # Should return True on success
        assert result in (True, None)  # Some implementations may return None

    def test_get_bound_driver(self, integration_client):
        """Test getting the bound WebDriver."""
        client = integration_client
        session = PulsarSession(client)
        
        driver = session.driver
        
        assert isinstance(driver, WebDriver)
        # Same instance should be returned
        assert session.driver is driver


@pytest.mark.integration
class TestWebDriverIntegration:
    """Integration tests for WebDriver with real server."""

    def test_navigate_to(self, integration_client):
        """Test WebDriver navigation."""
        client = integration_client
        driver = WebDriver(client)
        
        driver.navigate_to(SIMPLE_PAGE)
        
        # Should be able to get current URL
        url = driver.current_url()
        assert url is not None

    def test_navigate_and_check_url(self, integration_client):
        """Test navigation and URL retrieval."""
        client = integration_client
        driver = WebDriver(client)
        
        driver.navigate_to(SIMPLE_PAGE)
        current = driver.current_url()
        
        assert current is not None
        # URL might have been normalized, so just check it's not empty
        assert len(current) > 0

    def test_execute_script(self, integration_client):
        """Test JavaScript execution."""
        client = integration_client
        session = PulsarSession(client)
        driver = session.driver
        
        # Navigate to a page first
        session.open(SIMPLE_PAGE)
        
        # Execute simple script
        result = driver.execute_script("return document.title")
        
        # Should return something (even if empty string)
        assert result is not None

    def test_delay(self, integration_client):
        """Test delay functionality."""
        client = integration_client
        driver = WebDriver(client)
        
        # Should not raise an error
        driver.delay(100)  # 100ms delay

    def test_navigation_history(self, integration_client):
        """Test navigation history tracking."""
        client = integration_client
        driver = WebDriver(client)
        
        # Navigate to multiple pages
        driver.navigate_to(SIMPLE_PAGE)
        driver.navigate_to(PRODUCT_LIST)
        
        history = driver.navigate_history
        
        assert len(history) == 2
        assert SIMPLE_PAGE in history[0]
        assert PRODUCT_LIST in history[1]


@pytest.mark.integration
class TestAgenticSessionIntegration:
    """Integration tests for AgenticSession with real server."""

    def test_open_and_get_driver(self, integration_client):
        """Test opening page and getting driver in agentic session."""
        client = integration_client
        session = AgenticSession(client)
        
        # Open a page
        page = session.open(SIMPLE_PAGE)
        assert isinstance(page, WebPage)
        assert not page.is_nil
        
        # Get driver
        driver = session.get_or_create_bound_driver()
        assert isinstance(driver, WebDriver)
        assert driver is session.driver

    def test_full_workflow(self, integration_client):
        """Test a complete workflow similar to FusedActs."""
        client = integration_client
        session = AgenticSession(client)
        
        # Open URL
        page = session.open(SIMPLE_PAGE)
        assert page.url is not None
        
        # Get driver
        driver = session.get_or_create_bound_driver()
        assert driver is session.driver
        
        # Navigate to another page
        driver.navigate_to(PRODUCT_LIST)
        current_url = driver.current_url()
        assert current_url is not None
        
        # Check navigation history
        assert len(session.driver.navigate_history) > 0


@pytest.mark.integration
class TestMockServerAccess:
    """Test that mock server is accessible and providing content."""

    def test_mock_server_is_running(self, mock_server):
        """Verify mock server is accessible."""
        import urllib.request
        
        response = urllib.request.urlopen(MOCK_SERVER_BASE, timeout=5)
        assert response.status == 200

    def test_simple_page_accessible(self, mock_server):
        """Verify simple page is accessible."""
        import urllib.request
        
        response = urllib.request.urlopen(SIMPLE_PAGE, timeout=5)
        assert response.status == 200
        
        content = response.read()
        assert len(content) > 0

    def test_product_list_accessible(self, mock_server):
        """Verify product list page is accessible."""
        import urllib.request
        
        response = urllib.request.urlopen(PRODUCT_LIST, timeout=5)
        assert response.status == 200


if __name__ == "__main__":
    # Run integration tests with verbose output
    pytest.main([__file__, "-v", "-s", "-m", "integration"])
